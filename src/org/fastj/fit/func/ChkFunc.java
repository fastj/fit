package org.fastj.fit.func;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.model.Consts;
import org.fastj.fit.model.verify.VerifyTable;
import org.fastj.fit.tool.JSONHelper;

import static org.fastj.fit.tool.StringUtil.*;

public class ChkFunc implements IFunction{

	@Override
	public String name() {
		return "chk";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || (args.length != 1 && args.length != 3)) throw new DataInvalidException("Func[chk] requires 3 parameters.");
		
		if (args.length == 1)
		{
			args = readFuncParam(args[0], ' ');
		}
		
		if (args.length != 3) throw new DataInvalidException("Func[chk] requires 3 parameters.");
		
		VerifyTable vt = new VerifyTable();
		vt.add(args[0], args[1], args[2]);
		
		vt.fillValue(JSONHelper.getJson(expendVar("_resp_", ptable)), ptable);
		StepResult sr = vt.check();
		return String.valueOf(sr.getResult() == Consts.PASS);
	}

}
