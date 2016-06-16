package org.fastj.fit.fcall;

import java.util.Map;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.tool.JSONHelper;
import org.fastj.fit.tool.StringUtil;

public class EchoCall implements IFuncCall{

	@Override
	public String name() {
		return "echo";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		
		String response = StringUtil.expend(argStr, table);
		Map<String, Object> entity = JSONHelper.getJson(response);
		
		FuncResponse fr = new FuncResponse();
		fr.setCode(FuncResponse.OK);
		fr.setRequest("ECHO " + argStr);
		fr.setEntity(entity);
		
		return fr;
	}

}
