package org.fastj.fit.fcall;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.tool.StringUtil;

public class Delay implements IFuncCall{

	@Override
	public String name() {
		return "delay";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		String tstr = StringUtil.expend(argStr, table);
		if (tstr == null || tstr.trim().isEmpty()) {
			throw new DataInvalidException("FCall[delay] requires 1 parameter.");
		}
		
		int time = 0;
		try {
			time = Integer.valueOf(tstr.trim());
		} catch (NumberFormatException e) {
			throw new DataInvalidException("FCall[delay] parameter invalid.");
		}
		
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			LogUtil.debug("Delay interrupted");
		}
		table.add("logoff", "true");
		return new FuncResponse();
	}

}
