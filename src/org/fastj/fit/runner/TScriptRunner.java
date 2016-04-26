/*
 * Copyright 2015  FastJ
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastj.fit.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.Parameter;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.tool.TCSLoader;
import org.fastj.fit.tool.TRun;
import org.fastj.fit.tool.TSysInit;

/**
 * 测试脚本执行入口
 * 
 * @author zhouqingquan
 *
 */
public class TScriptRunner {
	
	public static void main(String[] args) throws DataInvalidException {
		run(args);
	}
	
	public static void run(String ... args) throws DataInvalidException
	{
		TProject tproj = new TProject();
		TSysInit.loadPlugins(tproj);
		
		ParameterTable argTable = new ParameterTable();
		List<String> filelist = new ArrayList<String>();
		
		for (String arg : args)
		{
			if (arg.startsWith("-P"))
			{
				String pv[] = arg.substring(2).split("=", 2);
				if (pv.length == 2)
				{
					argTable.add(pv[0], pv[1]);
				}
				else
				{
					argTable.add(pv[0], "true");
				}
			}
			else if (arg.startsWith("-f"))
			{
				filelist.add(arg.substring(2));
			}
		}
		
		init(tproj, filelist, argTable);
		
		LogUtil.level = tproj.getSysVars().getInt("loglevel", LogUtil.INFO);
		
		tproj.start();
		int maxL = tproj.getMaxLevel();
		for (int level = -1; level <= maxL; level++)
		{
			List<TSuite> tsl = tproj.getSuites(level);
			if (!tsl.isEmpty())
			{
				TRun.run(tproj, level, tsl);
			}
		}
		
		tproj.waitAll();
		
		//report
		if (tproj.getPostProc() != null)
		{
			tproj.getPostProc().end();
		}
		
		if (!RunMain.embedded){
			System.exit(0);
		}
	}
	
	public static void init(TProject tproj, List<String> files, ParameterTable args) throws DataInvalidException
	{
		tproj.setDir(args.getPara("project", "."));
		TCSLoader.load(tproj, files);
		
		for (Parameter p : args.gets())
		{
			tproj.getSysVars().add(p.getName(), p.getValue(), p.getDesc());
		}
		
		tproj.getSysVars().add("__run.dir__", new File("").getAbsolutePath());
	}
	
}
