package org.fastj.fit.runner;

import java.util.ArrayList;

import org.fastj.fit.func.Funcs;
import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.TSysInit;

public class UIRunner {
	
	public static void run(String[] args) throws DataInvalidException, ParamIncertitudeException {
		TSysInit.loadPlugins(null);
		Funcs.runFunc("fitUI", new ArrayList<String>(), new ParameterTable());
	}
	
}
