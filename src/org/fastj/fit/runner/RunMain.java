package org.fastj.fit.runner;

import org.fastj.fit.intf.DataInvalidException;

public class RunMain {

	public static void main(String[] args) throws DataInvalidException {
		
		for (String arg : args)
		{
			if ("-script".equals(arg))
			{
				TScriptRunner.run(args);
				return;
			}
		}
		
		TCaseRunner.main(args);
	}
	
}
