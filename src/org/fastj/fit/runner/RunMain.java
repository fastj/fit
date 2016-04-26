package org.fastj.fit.runner;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;

public class RunMain {

	public static boolean embedded = false;
	
	public static void main(String[] args) throws DataInvalidException, ParamIncertitudeException {
		
		for (String arg : args)
		{
			if ("-script".equals(arg))
			{
				TScriptRunner.run(args);
				return;
			}
			else if ("-case".equals(arg))
			{
				TCaseRunner.main(args);
				return;
			}
		}
		
		UIRunner.main(args);
	}
	
}
