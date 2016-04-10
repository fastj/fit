package org.fastj.fit.fcall;

import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.PostProc;
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.intf.TCNode;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.intf.TResult;
import org.fastj.fit.intf.TSuite;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.tool.StringUtil;

public class TRCall implements IFuncCall{

	@Override
	public String name() {
		return "report";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		
		String name = StringUtil.expendVar("title", table);
		String tid = StringUtil.expendVar("tid", table);
		
		TResult tr = new TResult();
		tr.setStart(ctx.getTestCase().getStartTime());
		tr.setEnd(ctx.getTestCase().getEndTime() > 0 ? ctx.getTestCase().getEndTime() : System.currentTimeMillis());
		
		tr.getLoopData().setParent(ctx.getOuts().copy());
		tr.getLoopData().getParent().setParent(table);
		
		String log = StringUtil.expendVar("log", table);
		tr.setLog(log);
		
		StepResult sr = new StepResult();
		String rltExpr = StringUtil.expendVar("rlt", table);
		sr.addMessage((Boolean.valueOf(rltExpr) ? "PASS" : "FAIL"));
		sr.setResult(Boolean.valueOf(rltExpr) ? TCNode.PASS : TCNode.FAIL);
		tr.mergeResult(sr);
		
		TCRNode tcase = new TCRNode(name, tid, ctx.getTestCase().getSuite() , tr);
		
		PostProc pp = ctx.getProject().getPostProc();
		if (pp != null){
			pp.start(tcase);
			pp.finish(tcase);
		}
		
		StepResult closeSr = new StepResult();
		closeSr.setResult(TCNode.REPLACED);
		closeSr.addMessage(sr.getMessages().get(0));
		ctx.getResult().mergeResult(closeSr);
		
		LogUtil.info(ctx.getLog().getLog());
		
		return new FuncResponse();
	}
	
	
	private static class TCRNode implements TCNode{

		long startTime = 0L;
		long endTime = 0L;
		String log = "";
		String name = "";
		String tid = "";
		int result = PASS;
		List<TResult> results = new ArrayList<>();
		TSuite suite = null;
		
		public TCRNode(String name, String tid, TSuite suite, TResult tr) {
			
			startTime = tr.getStart();
			endTime = tr.getEnd();
			log = tr.getLog();
			result = tr.getResult();
			results.add(tr);
			this.suite = suite;
			try {
				this.name = StringUtil.expend(name, tr.getLoopData());
			} catch (ParamIncertitudeException | DataInvalidException e) {
				this.name = name;
			}
			try {
				this.tid = StringUtil.expend(tid, tr.getLoopData());
			} catch (ParamIncertitudeException | DataInvalidException e) {
				this.tid = tid;
			}
		}
		
		@Override
		public long getEndTime() {
			return endTime;
		}

		@Override
		public String getLog() {
			return log;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getResult() {
			return result;
		}

		@Override
		public List<TResult> getResults() {
			return results;
		}

		@Override
		public long getStartTime() {
			return startTime;
		}

		@Override
		public TSuite getSuite() {
			return suite;
		}

		@Override
		public String getTid() {
			return tid;
		}

	}
	
	
	
}
