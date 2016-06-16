package org.fastj.fit.func;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;

public class NewLine implements IFunction{

	@Override
	public String name() {
		return "newline";
	}

	@Override
	public String frun(ParameterTable ptable, String... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (System.getProperty("os.name", "").toLowerCase().contains("win"))
		{
			return "\r\n";
		}
		
		return "\n";
	}

}
