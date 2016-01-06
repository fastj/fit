package org.fastj.fit.func;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.JS;
import org.fastj.fit.tool.StringUtil;

public class JSFunc implements IFunction{

	@Override
	public String name() {
		return "js";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 1) throw new DataInvalidException("Func[js] requires 1 parameter.");
		
		String jsStr = StringUtil.expend(args[0], ptable);
		Object v = JS.val(jsStr);
		return String.valueOf(v);
	}

}
