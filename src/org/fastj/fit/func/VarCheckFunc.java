package org.fastj.fit.func;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

public class VarCheckFunc implements IFunction{

	@Override
	public String name() {
		return "exist";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 1) throw new DataInvalidException("Func[exist] requires 1 parameter.");
		
		boolean b = ptable.gcontains(StringUtil.expend(args[0], ptable));
		
		return String.valueOf(b);
	}

}
