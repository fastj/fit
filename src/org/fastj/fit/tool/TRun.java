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
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.intf.TResult;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.log.NodeLogger;
import org.fastj.fit.model.Consts;
import org.fastj.fit.model.MStep;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.model.TestCase;
import org.fastj.fit.model.TestStep;
import org.fastj.fit.model.verify.VerifyTable;
import org.fastj.net.api.Response;
import org.fastj.net.api.SshConnection;
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
	
	public static void run(TProject tproj, int level, List<TSuite> tsl)
	{
		if (tsl.size() == 1)
		{
			new TSuiteTask(tsl.get(0), null).run();
			return;
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
			for (TestCase tc : suite.getTestCases())
			{
				try {
					String jsexpr = null;
					if (tc.getSkipExpr() != null && (boolean) JS.val(jsexpr = expend(tc.getSkipExpr(), tc.getParamTable())))
					{
						tc.getNLoggor().warn("  ===> TestCase [{}] skiped. jsExpr=[{}]", tc.getName(), jsexpr);
						continue;
					}
					else if (jsexpr != null)
					{
						tc.getNLoggor().warn("  ===> TestCase [{}] go test. jsExpr=[{}]", tc.getName(), jsexpr);
					}
				} catch (Throwable e1) {
					tc.getNLoggor().warn("  ===> TestCase [{}] check skipExpr fail and go test: {}", tc.getName(), EFormat.exStr(e1));
					TResult tr = new TResult();
					StepResult sr = new StepResult();
					sr.addMessage("Fail. " + EFormat.exStr(e1));
					sr.setResult(Consts.BLOCKED);
					tr.mergeResult(sr);
					tc.mergeResult(tr);
					continue;
				}
				
				try {
					TRun.run(tproj, tc);
				} catch (Throwable e) {
					TResult tr = new TResult();
					StepResult sr = new StepResult();
					sr.addMessage("Fail. " + EFormat.exStr(e));
					sr.setResult(Consts.BLOCKED);
					tr.mergeResult(sr);
					tc.mergeResult(tr);
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
	
	public static void run(TProject tproj, TestCase tc) throws ParamIncertitudeException, DataInvalidException
	{
		tc.setStartTime(System.currentTimeMillis());
		
		String lvars[] = loopVars(tc.getLoopVars());
		List<ParameterTable> lparas = splits(tc.getParamTable(), lvars);
		
		ExecutorService executor = Executors.newFixedThreadPool(tc.threadNum(lparas.size()));
		CountDownLatch cdl = new CountDownLatch(lparas.size());
		for (ParameterTable tctable : lparas)
		{
			TCaseTask tctask = new TCaseTask(tc, cdl, tctable);
			executor.submit(tctask);
		}
		
		int lv = tc.getWaitLevel();
		
		//if wait on level, and level is valid
		if (tc.isLevelWait() && lv >= tc.getOwner().getLevel())
		{
			tc.getProject().waitOnLevel(lv, cdl, executor, tc);
		}
		else
		{
			try { cdl.await(); } catch (InterruptedException e) {}
			executor.shutdown();
			
			//all copy is finished.
			if (tproj.getPostProc() != null)
			{
				tproj.getPostProc().finish(tc);
			}
		}
		
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
			TContext context = new TContext();
			NodeLogger nlog = new NodeLogger();
			context.put("_nlog_", nlog);
			context.put("__TR__", tr);
			context.put("__project__", tc.getProject());
			try {
				run(tc, ptable, context);
			} 
			catch (Throwable e)
			{
				StepResult sr = new StepResult();
				sr.addMessage("Fail. " + EFormat.exStr(e));
				sr.setResult(Consts.BLOCKED);
				tr.mergeResult(sr);
				
				tc.append(String.format("TestCase [%s] Exception occur: \r\n%s", tc.getName(), EFormat.exStr(e)));
			}
			finally
			{
				tc.mergeResult(tr);
				cdl.countDown();
				if (cdl.getCount() == 0)
				{
					tc.setEndTime(System.currentTimeMillis());
				}
				
				cleanResources(context);
				//log
				tc.append(nlog);
				
				nlog.trace(" ****** TestCase [{}] done, takes {} sec. Result ===> {}", tc.getName(), (System.currentTimeMillis() - start)/1000., tr.getResultDesc());
			}
		}
		
		private void cleanResources(TContext context)
		{
			SshConnection ssh = context.get("__ssh_connection__");
			if (ssh != null) ssh.close();
		}
		
		private void run(TestCase tc, ParameterTable tctable, TContext context) throws ParamIncertitudeException, DataInvalidException
		{
			NodeLogger nlog = context.get("_nlog_");
			
			nlog.info(" ****** Start run TestCase [{}] tid={}", tc.getName(), tc.getTid());
			TResult tr = context.get("__TR__");
			
			String jsexpr = null;
			if (tc.getSkipExpr() != null && (boolean) JS.val(jsexpr = expend(tc.getSkipExpr(), tctable)))
			{
				nlog.info("   ===> TestCase [{}] skiped: jsexpr=[{}]", tc.getName(), jsexpr);
			}
			else
			{
				boolean teardown = false;
				for (TestStep tstep : tc.getSteps())
				{
					if (!teardown || (teardown && tstep.getTestStage() == Consts.TSTAGE_TEARDOWN))
					try {
						if (tstep.getSkipExpr() != null && (boolean) JS.val(expend(tstep.getSkipExpr(), tctable)))
						{
							nlog.info("  ===> Step [{}] skiped.", tstep.getFuncCmd());
							continue;
						}
						runStep(tstep, context, tctable);
					} catch (Throwable e) {
						StepResult srlt = new StepResult();
						srlt.setRequest(tstep.getFuncCmd());
						String str = "Fail. " + EFormat.exStr(e);
						srlt.setResponse(str);
						srlt.addMessage(str);
						srlt.setResult(Consts.BLOCKED);
						tstep.mergeSResult(srlt);
						teardown = true;
						
						nlog.info("Step [{}] Exception occur: \r\n{}", tstep.getFuncCmd(), EFormat.exStr(e));
						nlog.error("Step Blocked.");
					}
					finally
					{
						tr.mergeResult(tstep.getResults());
					}
				}
			}
			
		}
		
	}//TCaseTask
	
	private static void runStep(TestStep step, TContext ctx, ParameterTable tctable) throws ParamIncertitudeException, DataInvalidException 
	{
		ParameterTable stepTable = step.getParamTable().copy();
		stepTable.setParent(tctable);
		List<ParameterTable> stl = splits(stepTable, splits(step.getLoopVars()));

		ExecutorService executor = Executors.newFixedThreadPool(step.threadNum(stl.size()));
		
		try {
			CountDownLatch scdl = new CountDownLatch(stl.size());
			for (ParameterTable stable : stl)
			{
				StepTask stask = new StepTask(step, scdl, stable, ctx);
				executor.submit(stask);
			}
			scdl.await();
		} 
		catch (Throwable e){
			StepResult srlt = new StepResult();
			srlt.setRequest(step.getFuncCmd());
			String str = "Fail. " + EFormat.exStr(e);
			srlt.setResponse(str);
			srlt.addMessage(str);
			srlt.setResult(Consts.BLOCKED);
			step.mergeSResult(srlt);
			
			NodeLogger nlog = ctx.get("_nlog_");
			nlog.error("  Run step [{}] error: {}", step.getFuncCmd(), EFormat.exStr(e));
		}
		finally
		{
			executor.shutdown();
		}
	}//runStep
	
	private static class StepTask implements Runnable
	{
		TestStep step;
		CountDownLatch cdl;
		ParameterTable ptable;
		TContext context = null;
		
		public StepTask(TestStep step, CountDownLatch cdl, ParameterTable ptable, TContext tcontext) {
			this.step = step;
			this.cdl = cdl;
			this.ptable = ptable;
			this.context = tcontext;
		}
		
		@Override
		public void run() {
			try {
				runStep(step, context, ptable);
			} catch (Throwable e) {
				StepResult srlt = new StepResult();
				srlt.setRequest(step.getFuncCmd());
				String str = "Fail. " + EFormat.exStr(e);
				srlt.setResponse(str);
				srlt.addMessage(str);
				srlt.setResult(Consts.BLOCKED);
				step.mergeSResult(srlt);
				
				NodeLogger nlog = context.get("_nlog_");
				nlog.error("  Run step [{}] error: {}", step.getFuncCmd(), EFormat.exStr(e));
				
			}
			finally
			{
				cdl.countDown();
			}
		}
		
		public void runStep(TestStep step, TContext ctx, ParameterTable ptable) throws ParamIncertitudeException, DataInvalidException 
		{
			long waitfor = step.getWaitfor();
			
			//delay
			sleep(step.getDelay());
			
			long endTime = System.currentTimeMillis() + waitfor;
			if (!CallUtil.isMStep(step.getFuncCmd()))
			{
				do
				{
					StepResult sr = stepFuncRun(step, ctx, ptable);
					
					if (sr.isPass() || sr.isBlock() || sr.isFastFail() || System.currentTimeMillis() >= endTime)
					{
						step.mergeSResult(sr);
						break;
					}
					
					sleep(step.getInternal());
				}while(endTime > System.currentTimeMillis());
			}
			else
			{
				MStep mstep = CallUtil.getMStep(step.getFuncCmd());
				runMStep(step, mstep, ctx, ptable);
			}
			
		}
	}
	
	private static StepResult stepFuncRun(TestStep step, TContext ctx, ParameterTable ptable) throws ParamIncertitudeException, DataInvalidException 
	{
		NodeLogger log = new NodeLogger();
		log.info("  ====== Start run step ===> {}", step.getFuncCmd());
		long stepStart = System.currentTimeMillis();
		FuncResponse fresp = CallUtil.run(step, ctx, ptable);
		
		double cost = (System.currentTimeMillis() - stepStart)/1000.;
		log.info("    REQ: {}", fresp.getRequest());
		log.info("    RESP: \r\n{}", JSONHelper.jsonString(fresp.getEntity()));
		
		VerifyTable vft = step.getVfTable().copy();
		vft.fillValue(fresp.getEntity(), ptable);
		
		StepResult sr = vft.check();
		sr.setCost((float) cost);
		if (fresp.getCode() != Response.OK)	
		{
			sr.addMessage(fresp.getPhrase());
			log.error("    Exception Message: ", fresp.getPhrase());
		}
		
		for (String msg : sr.getMessages())
		{
			log.info("    Check: {}", msg);
		}
		
		sr.setRequest(fresp.getRequest());
		sr.setResponse(JSONHelper.jsonString(fresp.getEntity()));
		if (sr.isPass())
		{
			for (String ostr : step.getOutCmdLines())
			{
				String [] par = ostr.split("," , 2);
				if (par.length == 1) {
					throw new DataInvalidException("Invalid out expr: " + ostr);
				}
				String name = expend(par[0].trim(), ptable);
				String path = expend(par[1].trim(), ptable);
				
				String pvalue = null;
				if (path.startsWith("json."))
				{
					Object ov = JSONHelper.jsonValue(path, fresp.getEntity());
					pvalue = ov == null || ov instanceof String ? String.valueOf(ov) : JSONHelper.jsonString(ov);
				}
				else
				{
					pvalue = path;
				}
				
				log.info("    Out param ===> {} = {}", name, pvalue);
				step.getOwner().getProject().getSysVars().add(name, pvalue);
			}
		}
		
		log.info("  ====== Step takes {} sec. Result ==> {}", sr.getCost(), sr.isPass() ? "OK" : sr.isBlock() ? "BLOCK" : "FAIL");
		NodeLogger nlog = ctx.get("_nlog_");
		nlog.append(log);
		return sr;
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
}
