package org.fastj.fit.fcall;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.PostProc;
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.intf.TResult;
import org.fastj.fit.model.TestCase;
import org.fastj.fit.tool.StringUtil;

public class TRCall implements IFuncCall{

	@Override
	public String name() {
		return "report";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		
		TestCase tc = new TestCase();
		
		tc.setName(StringUtil.expendVar("title", table));
		tc.setTid(StringUtil.expendVar("tid", table));
		tc.setStartTime(ctx.getTestCase().getStartTime());
		tc.setEndTime(ctx.getTestCase().getEndTime() > 0 ? ctx.getTestCase().getEndTime() : System.currentTimeMillis());
		
		TResult tr = new TResult();
		StepResult sr = new StepResult();
		
		String rltExpr = StringUtil.expendVar("rlt", table);
		sr.addMessage((Boolean.valueOf(rltExpr) ? "PASS" : "FAIL"));
		tr.mergeResult(sr);
		
		tc.getNLoggor().append(StringUtil.expendVar("log", table));
		
		PostProc pp = ctx.getProject().getPostProc();
		if (pp != null){
			pp.start(tc);
			pp.finish(tc);
		}
		
		return new FuncResponse();
	}
	
	
	
	
	
	
}
