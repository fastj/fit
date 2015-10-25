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

package org.fastj.fit.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.fcall.CallUtil;
import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.Consts;
import org.fastj.fit.model.MStep;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.model.TestCase;
import org.fastj.fit.model.TestStep;

/**
 * FIT Loader
 * 
 * @author zhouqingquan
 *
 */
public class TCSLoader {
	
	public static void load(TProject tproj, List<String> files) throws DataInvalidException
	{
		String constsFile = TProject.CONSTS_FILE;
		List<File> scripts = new ArrayList<File>();
		for (String f : files)
		{
			if (f.startsWith("consts_"))
			{
				constsFile = f;
			}
			else
			{
				File file = null;
				if ((file = tproj.getProjectFile(f)) != null)
				{
					scripts.add(file);
				}
			}
		}
		files.remove(constsFile);
		tproj.setConsts(constsFile);
		
		loadConsts(tproj.getConsts(), tproj.getSysVars());
		scripts = scripts.isEmpty() ? tproj.getProjectFiles() : scripts;
		
		for (File script : scripts)
		{
			loadScripts(script, tproj);
		}
		
		for (File aws : tproj.getAWscripts())
		{
			loadAWScripts(aws);
		}
	}
	
	private static void loadScripts(File sf, TProject tproj) throws DataInvalidException
	{
		int lineTag = 0;
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(sf))){
			lineTag++;
			TSuite suite = TSuite.create(sf.getName());
			suite.setProject(tproj);
			tproj.addSuite(suite);
			
			TestCase tcase = null;
			TestStep step = null;
			
			boolean comment = false;
			while((line = br.readLine()) != null)
			{
				line = line.trim();
				
				if (line.isEmpty() || line.startsWith("//"))
				{
					continue;
				}
				
				if (line.startsWith("/*"))
				{
					comment = true;
				}
				
				if (comment || line.endsWith("*/"))
				{
					comment = !line.endsWith("*/");
					continue;
				}
				
				if (line.startsWith(";---") || line.startsWith("#---"))
				{
					if (tcase != null)
					{
						tcase.valid();
						tcase.initTStage();
						tcase = null;
						step = null;
					}
					continue;
				}
				
				if (line.startsWith("#") || line.startsWith(";"))
				{
					continue;
				}
				
				if (line.matches(Consts.IMPORT_CONSTS_PATTERN))
				{
					String fname = line.split("[\\s]{1,}")[1];
					LogUtil.trace("Load consts from {} to {}", fname, suite.getName());
					loadConsts(new File(fname), suite.getParamTable());
					continue;
				}
				
				if (tcase == null)
				{
					tcase = new TestCase();
					tcase.setOwner(suite);
					tcase.setProject(tproj);
				}
				
				//test step function
				if (line.matches(Consts.STEP_FUNC_PATTERRN))
				{
					step = new TestStep();
					tcase.addStep(step);
					step.setOwner(tcase);
					step.setFuncCmd(line.substring(1));
					continue;
				}
				
				// normal function response 
				if (line.matches(Consts.FUNC_PATTERRN))
				{
					parseFuncLine(line, tcase, step, lineTag);
					continue;
				}//TS/TC Function expr
				
				//Parameter setting
				parseParaLine(line, tcase, step, lineTag);
				
				//Unknown line, take as document
			}
		} 
		catch(DataInvalidException de)
		{
			LogUtil.error("DataInvalidException {}@{} : {}", sf.getName(), lineTag, line);
			throw de;
		}
		catch (Exception e) {
			LogUtil.error("Line parse {}:{}, {}@{} : {}", e.getClass().getName(), e.getMessage(), sf.getName(), lineTag, line);
		}
		
	}//End of loadScripts
	
	private static void parseParaLine(String line, TestCase tcase, TestStep step, int lineTag) throws DataInvalidException, ParamIncertitudeException
	{
		int idx = -1;
		if ((idx = line.indexOf('=')) > 0)
		{
			String name = StringUtil.trim(line.substring(0, idx));
			String value = StringUtil.trim(line.substring(idx + 1));
			if ("title".equals(name))
			{
				if (tcase == null) throw new DataInvalidException("Cannot set testcase property in AW scripts.");
				tcase.setName(value);
				return;
			}
			else if ("tid".equals(name))
			{
				if (tcase == null) throw new DataInvalidException("Cannot set testcase property in AW scripts.");
				tcase.setTid(value);
				return;
			}
			
			if (step != null)
			{
				step.getParamTable().add(name, value);
			}
			else
			{
				if (tcase == null) throw new DataInvalidException("Cannot set testcase property in AW scripts.");
				tcase.getParamTable().add(name, value);
			}
		}
	}
	
	private static void parseFuncLine(String line, TestCase tcase, TestStep step, int lineTag) throws DataInvalidException, ParamIncertitudeException
	{
		//TS support out(), chk(), waitfor(), delay(), loop(), skip(), thread()
		//TC support loop thread switch
		int idx = line.indexOf('(');
		String fname = line.substring(0, idx).trim();
		String fbody = line.substring(idx + 1, line.length() - 1).trim();
		
		switch (fname) {
		case "block":
			step.setTestStage(Consts.TSTAGE_PRE);
			break;
		case "teardown":
			step.setTestStage(Consts.TSTAGE_TEARDOWN);
			break;
		case "out": 
			step.addOut(fbody);
			break;
		case "chk": 
			String [] pars = fbody.split(",", 3);
			if (pars.length != 3)
			{
				pars = StringUtil.readCmdParam(fbody, false);
			}
			
			if (pars.length == 3)
			{
				step.getVfTable().add(pars[0].trim(), pars[1].trim(), pars[2].trim());
			}
			else
			{
				throw new DataInvalidException("Invalid CheckPoint @line:" + lineTag);
			}
			break;
		case "waitfor":
			String [] tpars = fbody.split(",");
			if (tpars.length <= 2)
			{
				try {
					step.setWaitfor(Integer.valueOf(StringUtil.trim(StringUtil.expend(tpars[0], step.getParamTable()))));
					step.setInternal(tpars.length > 1 ? Integer.valueOf(StringUtil.trim(StringUtil.expend(tpars[1], step.getParamTable()))) : 3000);
				} catch (NumberFormatException e) {
					throw new DataInvalidException("Invalid Waitfor @line:" + lineTag);
				}
			}
			else
			{
				throw new DataInvalidException("Invalid Waitfor @line:" + lineTag);
			}
			break;
		case "delay":
			try {
				step.setDelay(Integer.valueOf(StringUtil.trim(StringUtil.expend(fbody, step.getParamTable()))));
			} catch (NumberFormatException e) {
				throw new DataInvalidException("Invalid delay @line:" + lineTag);
			}
			break;
		case "loop":
			if (step != null)
			{
				step.setLoopVars(StringUtil.trim(fbody));
			}
			else
			{
				if (tcase != null)
					tcase.setLoopVars(StringUtil.trim(fbody));
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		case "skip":
			if (step != null)
			{
				step.setSkipExpr(StringUtil.trim(fbody));
			}
			else
			{
				if (tcase != null)
					tcase.setSkipExpr(StringUtil.trim(fbody));
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		case "thread":
			if (step != null)
			{
				step.setThread(Integer.valueOf(StringUtil.trim(fbody)));
			}
			else
			{
				if (tcase != null)
					tcase.setThread(Integer.valueOf(StringUtil.trim(fbody)));
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		default:
			if (step != null)
			{
				step.addParameter(fname, fbody);
			}
			else
			{
				if (tcase != null)
					tcase.addParameter(fname, fbody);
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		}
	}
	
	private static void loadConsts(File constsFile, ParameterTable table)
	{
		File cf = constsFile;
		try (BufferedReader br = new BufferedReader(new FileReader(cf))){
			String line = null;
			boolean comment = false;
			while((line = br.readLine()) != null)
			{
				line = line.trim();
				
				if (line.startsWith("#") || line.startsWith("//"))
				{
					continue;
				}
				
				if (line.startsWith("/*"))
				{
					comment = true;
				}
				
				if (comment || line.endsWith("*/"))
				{
					comment = !line.endsWith("*/");
					continue;
				}
				
				int idx = -1;
				if ((idx = line.indexOf('=')) > 0)
				{
					String name = StringUtil.trim(line.substring(0, idx));
					name = name.startsWith("sys.para.") ? name.substring(9) : name;
					table.add(name, StringUtil.trim(line.substring(idx + 1)));
				}
			}
		} catch (Exception e) {
			LogUtil.error("Load consts fail: {}", e.getMessage());
		}
	}//End of loadConsts()
	
	
	static final String DEF_PATTERN = "^def[ ]{1,}([a-zA-Z0-9_]{1,128})\\(([\\S\\s]*)\\)$";
	private static void loadAWScripts(File awFile)
	{
		File cf = awFile;
		int lineTag = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(cf))){
			String line = null;
			boolean comment = false;
			MStep mstep = null;
			TestStep tstep = null;
			while((line = br.readLine()) != null)
			{
				line = line.trim();
				lineTag++;
				if (line.startsWith("#") || line.startsWith("//"))
				{
					continue;
				}
				
				if (line.startsWith("/*"))
				{
					comment = true;
				}
				
				if (comment || line.endsWith("*/"))
				{
					comment = !line.endsWith("*/");
					continue;
				}
				
				if (line.matches(DEF_PATTERN))
				{
					Matcher m = Pattern.compile(DEF_PATTERN).matcher(line);
					m.find();
					String awName = m.group(1);
					String paramstr = m.group(2);
					
					mstep = new MStep();
					mstep.setName(awName);
					CallUtil.registAW(mstep);
					String params[] = StringUtil.readFuncParam(paramstr);
					for (String para : params)
					{
						mstep.addParam(para);
					}
					continue;
				}
				
				//test step function
				if (line.matches(Consts.STEP_FUNC_PATTERRN))
				{
					tstep = new TestStep();
					mstep.addStep(tstep);
					tstep.setFuncCmd(line.substring(1));
					continue;
				}
				
				// normal function response 
				if (line.matches(Consts.FUNC_PATTERRN))
				{
					parseFuncLine(line, null, tstep, lineTag);
					continue;
				}//TS/TC Function expr
				
				//Parameter setting
				parseParaLine(line, null, tstep, lineTag);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.error("Load aws fail: {}", e.getMessage());
		}
	}//End of loadConsts()
	
}
