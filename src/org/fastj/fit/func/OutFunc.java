package org.fastj.fit.func;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

public class OutFunc implements IFunction{

	@Override
	public String name() {
		return "out";
	}

	@Override
	public String frun(ParameterTable table, String... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length != 2) {
			throw new DataInvalidException("Func[out] requires 2 parameters.");
		}
		
		String varName = StringUtil.expend(args[0], table).trim();
		String expV = StringUtil.expend(args[1], table);
		table.add(varName, expV);
		
		return expV;
	}

}
