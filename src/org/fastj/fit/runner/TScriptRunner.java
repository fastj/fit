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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.fcall.CallUtil;
import org.fastj.fit.func.Funcs;
import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.PostProc;
import org.fastj.fit.jenkins.JenkinsPostProc;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.tool.PCLoader;
import org.fastj.fit.tool.TRun;

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
		try {
			changeCL();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			LogUtil.error("Set ext classloader failed: {}", e.getMessage());
		}
		
		ParameterTable argTable = new ParameterTable();
		List<String> filelist = new ArrayList<String>();
		PostProc postProc = new JenkinsPostProc();
		for (String arg : args)
		{
			if (arg.startsWith("-P"))
			{
				String pv[] = arg.substring(2).split("=", 2);
				if (pv.length == 2)
				{
					argTable.add(pv[0], pv[1]);
				}
			}
			else if (arg.startsWith("-f"))
			{
				filelist.add(arg.substring(2));
			}
			else if (arg.startsWith("-I"))
			{
				String clazz = arg.substring(2);
				Class<?> c = PCLoader.load(clazz);
				Object iobj = null;
				try {
					iobj = c.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					LogUtil.error("Plugin load fail: {}", e, clazz);
					continue;
				}
				
				if (iobj instanceof PostProc)
				{
					postProc = (PostProc) iobj;
				}
				else if (iobj instanceof IFuncCall)
				{
					CallUtil.regist((IFuncCall) iobj);
				}
				else if (iobj instanceof IFunction)
				{
					Funcs.regist((IFunction) iobj);
				}
			}
		}
		
		TProject tproj = TProject.init(filelist, argTable);
		tproj.setPostProc(postProc);
		
		LogUtil.level = tproj.getSysVars().getInt("loglevel", LogUtil.INFO);
		
		int maxL = tproj.getMaxLevel();
		for (int level = 0; level <= maxL; level++)
		{
			List<TSuite> tsl = tproj.getSuites(level);
			TRun.run(tproj, level, tsl);
		}
		
		//report
		if (tproj.getPostProc() != null)
		{
			tproj.getPostProc().end();
		}
		
		System.exit(0);
	}
	
	private static void changeCL() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		ClassLoader curr = TScriptRunner.class.getClassLoader();
		ClassLoader currParent = curr.getParent();
		ClassLoader pcl = PCLoader.initCL("ext", currParent);
		
		Field pf = ClassLoader.class.getDeclaredField("parent");
		pf.setAccessible(true);
		pf.set(curr, pcl);
	}
	
}
