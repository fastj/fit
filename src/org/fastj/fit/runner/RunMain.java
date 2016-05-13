package org.fastj.fit.runner;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;

public class RunMain {

	public static boolean embedded = false;
	
	public static void main(String[] args) throws DataInvalidException, ParamIncertitudeException {
		
		if (args == null || args.length == 0) {
			UIRunner.run(args);
			return;
		}
		
		for (String arg : args)
		{
			if ("-script".equals(arg))
			{
				TScriptRunner.run(args);
				return;
			}
			else if ("-case".equals(arg))
			{
				new TCaseRunner().run(args);
				return;
			}
		}
		
		new TCaseRunner().run(args);
	}
	
}
