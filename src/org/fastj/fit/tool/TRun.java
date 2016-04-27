/*
 * Copyright 2015  FastJ
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastj.fit.tool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.fcall.CallUtil;
import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.PerfStat;
import org.fastj.fit.intf.Response;
import org.fastj.fit.intf.ScheduleTask;
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.intf.TCNode;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.intf.TResult;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.log.NodeLogger;
import org.fastj.fit.model.Consts;
import org.fastj.fit.model.MStep;
import org.fastj.fit.model.Schedule;
import org.fastj.fit.model.TOut;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.model.TestCase;
import org.fastj.fit.model.TestStep;
import org.fastj.fit.model.verify.VerifyTable;
import static org.fastj.fit.tool.StringUtil.*;

/**
 * FIT执行控制
 * 
 * 如无特殊需求，不要修改本类
 * 
 * @author zhouqingquan
 *
 */
public final class TRun {
	
	private static ExecutorService gexecutor = Executors.newCachedThreadPool();
	
	public static void run(TProject tproj, int level, List<TSuite> tsl)
	{
		if (tsl.size() == 1)
		{
			new TSuiteTask(tsl.get(0), null).run();
		}
		else if (tsl.size() > 1)
		{
			ExecutorService executor = Executors.newFixedThreadPool(tsl.size());
			CountDownLatch cdl = new CountDownLatch(tsl.size());
			
			for (TSuite ts : tsl)
			{
				executor.submit(new TSuiteTask(ts, cdl));
			}
			
			try {
				cdl.await();
			} catch (Throwable e) {
				LogUtil.error("Run Level {} fail: {}", tsl.get(0).getLevel(), e.getMessage());
			}
		}
		
		//wait on special case
		tproj.levelWait(level);
	}
	
	private static class TSuiteTask implements Runnable
	{
		TSuite suite;
		CountDownLatch cdl;
		TProject tproj = null;
		
		TSuiteTask(TSuite ts, CountDownLatch cdl)
		{
			this.suite = ts;
			this.cdl = cdl;
			tproj = suite.getProject();
		}
		
		public void run()
		{
			for (TestCase tcase : suite.getTestCases())
			{
				TestCase tc = tcase.copy();
				try {
					TRun.run0(tproj, tc, null);
				} catch (Throwable e) {
					TResult tr = new TResult();
					StepResult sr = new StepResult();
					sr.setCost(0);
					sr.setStart(System.currentTimeMillis());
					sr.addMessage("Fail. " + EFormat.exStr(e));
					sr.setResult(Consts.BLOCKED);
					tr.mergeResult(sr);
					tc.mergeResult(tr);
					
					tc.getNLoggor().error("", e);
					
					//case failed
					if (tproj.getPostProc() != null)
					{
						try {
							tproj.getPostProc().finish(tc);
						} catch (Exception e1) {
							LogUtil.error("Call postProc[finishTC2] fail: ", e1);
						}
					}
				}
			}
			
			if (cdl != null)
			{
				cdl.countDown();
			}
			
			//TSuite may be not finished.
//			if (tproj.getPostProc() != null)
//			{
//				tproj.getPostProc().finish(suite);
//			}
		}
	}
	
	public static void run(TProject tproj, TestCase tc, ParameterTable adds) throws ParamIncertitudeException, DataInvalidException
	{
		run0(tproj, tc.copy(), adds);
	}
	
	private static void run0(TProject tproj, TestCase tc, ParameterTable adds) throws ParamIncertitudeException, DataInvalidException
	{
		tc.setStartTime(System.currentTimeMillis());
		if (tproj.getPostProc() != null){
			tproj.getPostProc().start(tc);
		}
		
		ParameterTable tcpara = tc.getParamTable().copy();
		if (adds != null)
		{
			tcpara.addAll(adds);
		}
		
		String lvars[] = splits(tc.getLoopVars(), false);
		List<ParameterTable> lparas = splits(tcpara, lvars);
		
		Schedule schedule = tc.getSchedule();
		if (schedule != null) schedule.setTotal(lparas.size());
		ScheduleTask stask = null;
		
		ExecutorService executor = schedule == null ? Executors.newFixedThreadPool(tc.threadNum(lparas.size()))
                                                    : gexecutor;
		CountDownLatch cdl = new CountDownLatch(schedule != null ? schedule.getTotal() : lparas.size());
		if (schedule != null)
		{
			TcaseScheduleTask tst = new TcaseScheduleTask();
			tst.executor = executor;
			tst.scdl = cdl;
			tst.schedule = schedule;
			tst.stl = lparas;
			tst.tcase = tc;
			executor.execute(tst);
		}
		else
		{
			for (ParameterTable tctable : lparas)
			{
				TCaseTask tctask = new TCaseTask(tc, cdl, tctable);
				executor.submit(tctask);
			}
		}
		
		int lv = tc.getWaitLevel();
		
		//if wait on level, and level is valid
		if (tc.isLevelWait() && lv >= tc.getOwner().getLevel())
		{
			tc.getProject().waitOnLevel(lv, cdl, executor, tc, stask);
		}
		else
		{
			try { cdl.await(); } catch (InterruptedException e) {} finally { tc.setEndTime(System.currentTimeMillis()); }
			if (schedule != null) executor.shutdown();
			
			//all copy is finished.
			if (tproj.getPostProc() != null)
			{
				try {
					tproj.getPostProc().finish(tc);
				} catch (Exception e) {
					LogUtil.error("Call postProc[finishTC1] fail: ", e);
				}
			}
			
			//regist heartbeat
			if (tc.getParamTable().lcontains("heartbeat"))
			{
				//heartbeat=60000[,false]
				HBTask.registHB(tc, tc.getParamTable().getPara("heartbeat", null));
			}
		}
		
	}
	
	private static boolean skip(TestCase tc, ParameterTable table, TResult tr) {
		try {
			String jsexpr = tc.getSkipExpr();
			if (jsexpr != null) {
				Object ro = JS.val(jsexpr = expend(tc.getSkipExpr(), table));
				if (ro instanceof Boolean && (Boolean) ro) {
					tc.getNLoggor().warn("===> TestCase [{}] skiped. jsExpr=[{}]", tc.getName(), jsexpr);
					StepResult sr = new StepResult();
					sr.addMessage("Test Skipped: " + jsexpr);
					sr.setResult(Consts.SKIPPED);
					tr.mergeResult(sr);

					tr.setLog("Test Skipped: " + jsexpr);

					return true;
				} else if (!(ro instanceof Boolean)) {
					tc.getNLoggor().warn("===> TestCase [{}] jsExpr invalid: [{}]", tc.getName(), jsexpr);
				}
			}
		} catch (ParamIncertitudeException pe) {
			// skip expr fail, ignore and go test
		} catch (Throwable e1) {
			tc.getNLoggor().warn("===> TestCase [{}] check skipExpr fail and go test: {}", tc.getName(),
					EFormat.exStrEx(e1, true));
			StepResult sr = new StepResult();
			sr.addMessage("Fail. " + EFormat.exStr(e1));
			sr.setResult(Consts.BLOCKED);
			tr.mergeResult(sr);

			tr.setLog(EFormat.exStrEx(e1, true));
			
			return true;
		}
		
		return false;
	}
	
	private static class TCaseTask implements Runnable
	{
		TestCase tc;
		CountDownLatch cdl;
		ParameterTable ptable;
		
		TCaseTask(TestCase tcase, CountDownLatch cdl, ParameterTable ptable)
		{
			tc = tcase;
			this.cdl = cdl;
			this.ptable = ptable;
		}

		@Override
		public void run() {
			long start = System.currentTimeMillis();
			TResult tr = new TResult();
			tr.setStart(start);
			TContext context = new TContext();
			NodeLogger nlog = new NodeLogger();
			context.setLog(nlog);
			context.setResult(tr);
			context.setProject(tc.getProject());
			context.setTestCase(tc);
			try {
				if (!skip(tc, ptable, tr))
				{
					run(tc, ptable, context);
				}
			} 
			catch (Throwable e)
			{
				StepResult sr = new StepResult();
				sr.addMessage("Fail. " + EFormat.exStr(e));
				sr.setResult(Consts.BLOCKED);
				tr.mergeResult(sr);
				
				nlog.error("TestCase {} Exception occur:", e, tc.getName());
			}
			finally
			{
				try {
					tr.setEnd(System.currentTimeMillis());
					tc.mergeResult(tr);
					tr.setLog(nlog.getLog());
					try {
						fillLoopData(tr, context, tc, ptable);
					} catch (DataInvalidException | ParamIncertitudeException e) {
						nlog.error("TR fillLoopData(InternalERROR): {}", e.getMessage());
					}
					context.closeResources();
					//log
					try {
						nlog.trace("****** TestCase [{}] done, takes {} sec. Result ===> {}", expend(tc.getName(), ptable), (System.currentTimeMillis() - start) / 1000., tr.getResultDesc());
					} catch (ParamIncertitudeException | DataInvalidException e) {
						nlog.trace("****** TestCase [{}] done, takes {} sec. Result ===> {}", tc.getName(), (System.currentTimeMillis() - start) / 1000., tr.getResultDesc());
					}
					if (tc.getTid() == null || tc.getTid().indexOf("${") < 0) {
						tc.append(nlog);
					} 
				} finally {
					cdl.countDown();
				}
			}
		}//run
		
		private void run(TestCase tc, ParameterTable tctable, TContext context) throws ParamIncertitudeException, DataInvalidException
		{
			NodeLogger nlog = context.getLog();
			
			nlog.info("****** Start run TestCase [{}] tid={}", expend(tc.getName(), tctable), tc.getTid());
			TResult tr = context.getResult();
			
			boolean teardown = false;
			int preStage = Consts.TSTAGE_PRE;
			int currStage = Consts.TSTAGE_PRE;

			int skippedCnt = 0;
			for (TestStep step : tc.getSteps()) {
				TestStep tstep = step.copy();
				
				preStage = currStage;
				currStage = tstep.getTestStage();

				if (!tstep.isAsync() || (preStage != currStage)) {
					context.waitComplete();
				}

				teardown = (tr.getResult() != TestCase.PASS);

				boolean error = false;

				if (!teardown || (teardown && tstep.getTestStage() == Consts.TSTAGE_TEARDOWN))
					try {
						if (tstep.getSkipExpr() != null && (boolean) JS.val(expend(tstep.getSkipExpr(), tctable))) {
							nlog.trace("===> Step [{}] skiped.", tstep.getFuncCmd());
							skippedCnt++;
							continue;
						}

						runStep(tstep, context, tctable);
					} catch (Throwable e) {
						teardown = true;
						error = true;

						StepResult srlt = new StepResult();
						srlt.setRequest(tstep.getFuncCmd());
						String str = "Fail. " + EFormat.exStr(e);
						srlt.setResponse(str);
						srlt.addMessage(str);
						srlt.setResult(Consts.BLOCKED);
						tstep.mergeSResult(srlt);

						nlog.error("Step [{}] Exception occur: ", e, tstep.getFuncCmd());
						nlog.error("Step Blocked.");
					} finally {
						if (!tstep.isAsync() || error) {
							tr.mergeResult(tstep.getResults());
						}
					}
			} // for step

			// async wait if any
			context.waitComplete();
			
			//All steps skipped.
			if (skippedCnt == tc.getSteps().size())
			{
				StepResult sr = new StepResult();
				sr.setResult(TCNode.SKIPPED);
				sr.addMessage("All steps skipped.");
				tr.mergeResult(sr);
			}
		}
		
	}//TCaseTask
	
	public static void runStep(final TestStep step, final TContext ctx, ParameterTable tctable) throws ParamIncertitudeException, DataInvalidException 
	{
		ParameterTable stepTable = step.getParamTable().copy();
		ParameterTable ctxTable = ctx.getOuts().copy();
		ctxTable.setParent(stepTable);
		stepTable.setParent(tctable);
		List<ParameterTable> stl = splits(ctxTable, splits(step.getLoopVars(), false));

		Schedule schedule = step.getSchedule();
		if (schedule != null) schedule.setTotal(stl.size());
		ExecutorService executor = schedule == null ? Executors.newFixedThreadPool(step.threadNum(stl.size()))
				                                    : gexecutor;
		final PerfStat perf = stepTable.lcontains("perf") ? ctx.getProject().getPerfStat() : null;
		
		try {
			CountDownLatch scdl = new CountDownLatch(schedule != null ? schedule.getTotal() : stl.size());
			
			if (schedule != null)
			{
				StepScheduleTask stask = new StepScheduleTask();
				stask.ctx = ctx;
				stask.executor = executor;
				stask.scdl = scdl;
				stask.schedule = schedule;
				stask.step = step;
				stask.stl = stl;
				stask.perf = perf;
				executor.execute(stask);
				ctx.addSchedule(stask);
			}
			else  //submit all at once
			{
				for (ParameterTable stable : stl)
				{
					StepTask stask = new StepTask(step, scdl, stable, ctx, perf);
					executor.submit(stask);
				}
			}
			
			if (!step.isAsync())
			{
				scdl.await();
			}
			else
			{
				ctx.addAsync(scdl, executor, new Runnable() {
					public void run() {
						try {
							if (perf != null)
							{
								ctx.getLog().trace(perf.report(""));
							}
							ctx.getResult().mergeResult(step.getResults());
						} catch (Throwable e) {
							LogUtil.error("Merge step[{}] results fail: {}: {}", e, step.getFuncCmd(), e.getClass(), e.getMessage());
						}
					}
				}, schedule == null);
			}
		} 
		catch (Throwable e){
			StepResult srlt = new StepResult();
			srlt.setRequest(step.getFuncCmd());
			String str = "Fail. " + EFormat.exStr(e);
			srlt.setResponse(str);
			srlt.addMessage(str);
			srlt.setResult(Consts.BLOCKED);
			step.mergeSResult(srlt);
			
			NodeLogger nlog = ctx.getLog();
			nlog.error("Run step(TRun) [{}] error: ", step.getFuncCmd());
		}
		finally
		{
			if (!step.isAsync() && schedule == null) {
				executor.shutdown();
			}
		}
	}//runStep
	
	private static class StepTask implements Runnable
	{
		TestStep step;
		CountDownLatch cdl;
		ParameterTable ptable;
		TContext context = null;
		PerfStat perf = null;
		
		public StepTask(TestStep step, CountDownLatch cdl, ParameterTable ptable, TContext tcontext, PerfStat perf) {
			this.step = step;
			this.cdl = cdl;
			this.ptable = ptable;
			this.context = tcontext;
			this.perf = perf;
		}
		
		@Override
		public void run() {
			try {
				runStep(step, context, ptable);
			} catch (Throwable e) {
				NodeLogger nlog = context.getLog();
				StepResult srlt = new StepResult();
				srlt.setRequest(step.getFuncCmd());
				String str = "Fail. " + EFormat.exStr(e);
				srlt.setResponse(str);
				srlt.addMessage(str);
				srlt.setResult(Consts.BLOCKED);
				try {
					fillLoopData(srlt, context, step, ptable);
				} catch (DataInvalidException e1) {
					nlog.error("Run step(InternalERROR) [{}] DataInvalidException: {}", step.getFuncCmd(), e.getMessage());
				} catch (ParamIncertitudeException e1) {
					nlog.error("Run step(InternalERROR) [{}] ParamIncertitudeException: {}", step.getFuncCmd(), e.getMessage());
				}
				step.mergeSResult(srlt);
				
				
				nlog.error("Run step(T) [{}] error:", e, step.getFuncCmd());
			}
			finally
			{
				cdl.countDown();
			}
		}
		
		public void runStep(TestStep step, TContext ctx, ParameterTable tcenv) throws ParamIncertitudeException, DataInvalidException 
		{
			long waitfor = step.getWaitfor();
			
			//TestCase Environment Copy
//			ParameterTable tcenv = ptable;
//			tcenv.setParent(ptable);
			
			//delay
			sleep(step.getDelay());
			
			long endTime = System.currentTimeMillis() + waitfor;
			if (!CallUtil.isMStep(step.getFuncCmd()))
			{
				do
				{
					StepResult sr = stepFuncRun(step, ctx, tcenv);
					
					if (sr.isPass() || sr.isBlock() || sr.isFastFail() || System.currentTimeMillis() >= endTime)
					{
						fillLoopData(sr, ctx, step, ptable);
						step.mergeSResult(sr);
						if (perf != null) perf.put(step.getFuncCmd(), sr.getStart(), sr.getCost());
						break;
					}
					
					String lcondition = step.getLoopCondition();
					if (lcondition == null || (boolean) JS.val(expend(lcondition, tcenv)))
					{
						sleep(step.getInternal());
						continue;
					}
					else
					{
						break;
					}
					
				}while(endTime > System.currentTimeMillis());
			}
			else
			{
				MStep mstep = CallUtil.getMStep(step.getFuncCmd());
				runMStep(step, mstep, ctx, tcenv);
			}
			
		}//StepTask.runStep
	}//StepTask
	
	private static StepResult stepFuncRun(TestStep step, TContext ctx, ParameterTable ptable) throws ParamIncertitudeException, DataInvalidException 
	{
		NodeLogger log = new NodeLogger();
		log.trace("====== Start run step ===> {}", step.getFuncCmd());
		long stepStart = System.currentTimeMillis();
		FuncResponse fresp = CallUtil.run(step, ctx, ptable);
		
		if (fresp.getCode() == TCNode.REPLACED && fresp.getPhrase().startsWith("REPLACED:")){
			StepResult sr = new StepResult();
			sr.setResult(Boolean.valueOf(fresp.getPhrase().substring("REPLACED:".length()).trim()) ? TCNode.PASS : TCNode.FAIL);
			log.trace("====== Step takes {} msec. Create new TestCase Result ==> {}", sr.getCost(), sr.isPass() ? "PASS" : "FAIL");
			return sr;
		}
		else if (fresp.getCode() != Response.OK)
		{
			log.error("CallFail: {}", fresp.getPhrase());
		}
		
		String jsonEntity = JSONHelper.jsonString(fresp.getEntity());
		ptable.add("_resp_", jsonEntity);
		//For UI feature
		ptable.getParent().getParent().add("_resp_", jsonEntity);
		
		int cost = (int) (System.currentTimeMillis() - stepStart);
		
		VerifyTable vft = step.getVfTable().copy();
		vft.fillValue(fresp.getEntity(), ptable);
		
		StepResult sr = vft.check();
		sr.setStart(stepStart);
		sr.setCost(cost);
		
		if (!sr.isPass())
		{
			log.error("REQ: {}", fresp.getRequest());
			log.error("RESP: \r\n{}", jsonEntity);
		}else
		{
			log.info("REQ: {}", fresp.getRequest());
			log.info("RESP: \r\n{}", jsonEntity);
		}
		
		for (String msg : sr.getMessages())
		{
			log.info("Check: {}", msg);
		}
		
		sr.setRequest(fresp.getRequest());
		sr.setResponse(JSONHelper.jsonString(fresp.getEntity()));
		if (sr.isPass())
		{
			for (TOut to : step.getOutCmdLines())
			{
				String nv[] = out(to, ptable, fresp.getEntity(), log);
				String name = nv[0];
				String pvalue = nv[1];
				
				if (to.global)
				{
					step.getOwner().getProject().getSysVars().add(name, pvalue);
				}
				
				//TestCase copy scope
				ctx.out(name, pvalue);
				//TestStep copy scope
				ptable.add(name, pvalue);
			}
		}
		
		log.trace("====== Step takes {} msec. Result ==> {}", sr.getCost(), sr.isPass() ? "PASS" : sr.isBlock() ? "BLOCK" : "FAIL");
		NodeLogger nlog = ctx.getLog();
		nlog.append(log);
		return sr;
	}
	
	public static String[] out(TOut to, ParameterTable ptable, Map<String, Object> resp, NodeLogger log) throws ParamIncertitudeException, DataInvalidException{
		String name = expend(to.nameExpr, ptable);
		String path = expend(to.valueExpr, ptable);
		
		String pvalue = null;
		if (path.startsWith("json."))
		{
			Object ov = JSONHelper.jsonValue(path, resp);
			pvalue = ov == null || ov instanceof String ? String.valueOf(ov) : JSONHelper.jsonString(ov);
		}
		else
		{
			pvalue = path;
		}
		
		log.trace("Out param ===> {}@{} = {}", name, to.valueExpr, pvalue);
		
		return new String[]{name, pvalue};
	}
	
	private static void fillLoopData(StepResult sr, TContext ctx, TestStep tstep, ParameterTable table) throws DataInvalidException, ParamIncertitudeException
	{
		String varNames [] = splits(tstep.getLoopVars(), false);
		if (!ctx.getOuts().isEmpty())
		{
			ParameterTable ctxtable = ctx.getOuts().copy();
			ctxtable.setParent(table);
			sr.getLoopData().setParent(ctxtable);
		}
		else
		{
			sr.getLoopData().setParent(table);
		}
		
		for (String var : varNames)
		{
			String vname = expend(var, table);
			sr.getLoopData().add(vname, table.getPara(vname, "nil: Internal ERROR"));
		}
	}
	
	private static void fillLoopData(TResult tr, TContext ctx, TestCase tcase, ParameterTable table) throws DataInvalidException, ParamIncertitudeException{
		String varNames [] = splits(tcase.getLoopVars(), false);
		
		if (!ctx.getOuts().isEmpty())
		{
			ParameterTable ctxtable = ctx.getOuts().copy();
			ctxtable.setParent(table);
			tr.getLoopData().setParent(ctxtable);
		}
		else
		{
			tr.getLoopData().setParent(table);
		}
		
		for (String var : varNames)
		{
			String vname = expend(var, table);
			tr.getLoopData().add(vname, table.getPara(vname, "nil: Internal ERROR"));
		}
	}
	
	private static void runMStep(TestStep pstep, MStep mstep, TContext ctx, ParameterTable table) throws ParamIncertitudeException, DataInvalidException 
	{
		Matcher m = Pattern.compile(Consts.FUNC_PATTERRN).matcher(pstep.getFuncCmd());
		m.find();
		String paras = m.group(2).trim();
		
		String [] params = readFuncParam(paras);
		
		if (params.length < mstep.mSize())
		{
			throw new DataInvalidException("Must provide more parameters.");
		}
		
		ParameterTable currPTable = new ParameterTable();
		currPTable.setParent(table);
		int idx = 0;
		for (String pstr : params)
		{
			if (pstr.contains("=")){
				String pv[] = pstr.split("=", 2);
				currPTable.add(trim(pv[0]), pv[1]);
			}
			else
			{
				String pname = mstep.getParamName(idx);
				currPTable.add(pname, pstr);
			}
			
			idx++;
		}
		
		mstep.check(currPTable);
		
		for (TestStep istep : mstep.getSteps())
		{
			TestStep rStep = istep.copy();
			rStep.setOwner(pstep.getOwner());
			try {
				runStep(rStep, ctx, currPTable);
			} finally
			{
				for (StepResult sr : rStep.getResults())
				{
					pstep.mergeSResult(sr);
				}
			}
		}
	}
	
	private static final void sleep(long time)
	{
		if (time <= 0) return;
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}
	
	public static String[] loopVars(String loopVars) throws DataInvalidException
	{
		if (loopVars == null || loopVars.trim().isEmpty())
		{
			return new String[0];
		}
		
		return readFuncParam(loopVars);
	}
	
	private static class TcaseScheduleTask implements Runnable, ScheduleTask
	{
		Schedule schedule = null;
		ExecutorService executor = null;
		TestCase tcase;
		CountDownLatch scdl;
		List<ParameterTable> stl;
		
		boolean complete = false;
		
		public void run()
		{
			int lanched = 0;
			while(lanched < schedule.getTotal())
			{
				int lperl = schedule.getCntPerloop();
				long stime = System.currentTimeMillis();
				for (int i = 0; i < lperl; i++)
				{
					TCaseTask stask = new TCaseTask(tcase, scdl, stl.get(lanched++ % stl.size()));
					executor.submit(stask);
				}
				int tt = (int) (System.currentTimeMillis() - stime);
				sleep(schedule.getInterval() - tt);
			}
			complete = true;
		}

		@Override
		public boolean completed() {
			return complete;
		}
	}
	
	private static class StepScheduleTask implements Runnable, ScheduleTask
	{
		Schedule schedule = null;
		ExecutorService executor = null;
		TestStep step;
		CountDownLatch scdl;
		TContext ctx;
		List<ParameterTable> stl;
		PerfStat perf = null;
		
		boolean complete = false;
		
		public void run()
		{
			int lanched = 0;
			while(lanched < schedule.getTotal())
			{
				int lperl = schedule.getCntPerloop();
				long stime = System.currentTimeMillis();
				for (int i = 0; i < lperl; i++)
				{
					StepTask stask = new StepTask(step, scdl, stl.get(lanched++ % stl.size()), ctx, perf);
					executor.submit(stask);
				}
				int tt = (int) (System.currentTimeMillis() - stime);
				sleep(schedule.getInterval() - tt);
			}
			complete = true;
			ctx.rmvSchedule(this);
		}

		@Override
		public boolean completed() {
			return complete;
		}
	}
}
