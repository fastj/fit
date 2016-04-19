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
import java.io.FileNotFoundException;
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
import org.fastj.fit.model.Schedule;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TSuite;
import org.fastj.fit.model.TestCase;
import org.fastj.fit.model.TestStep;

import static org.fastj.fit.tool.StringUtil.*;

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
		
		File setup = tproj.getSetUp();
		if (setup.exists() && setup.isFile())
		{
			loadScripts(setup, tproj);
		}
		
		for (File script : scripts)
		{
			loadScripts(script, tproj);
		}
		
		for (File aws : tproj.getAWscripts())
		{
			loadAWScripts(aws);
		}
	}
	
	static final String TS_REGEX = "\\[[\\s]*L([0-9]{1,})[\\s]{1,}([\\S\\s]{1,})\\]";
	public static void loadV2(TProject tproj, File runfile) throws DataInvalidException
	{
		String dir = runfile.getParentFile() == null ? "." : runfile.getParentFile().getAbsolutePath();
		tproj.setDir(dir);
		tproj.getSysVars().add("__rt.dir__", dir);
		
		int lineTag = 0;
		String line = null;
		try (BufferedReader br = new BufferedReader(new FileReader(runfile))){
			TSuite suite = null;
			
			while((line = br.readLine()) != null)
			{
				lineTag++;
				line = line.trim();
				
				char c = line.isEmpty() ? ' ' : line.charAt(0);
				if (c == ' ' || c == '#' || c == '/' || c == '\\' || c == ';') continue;
				
				//Import Variables
				if (line.matches(Consts.IMPORT_CONSTS_PATTERN))
				{
					String fname = line.split("[\\s]{1,}", 2)[1];
					File cfile = tproj.getProjectFile(fname);
					if (cfile != null)
					{
						LogUtil.trace("Load consts from {} to {}", fname, suite != null ? suite.getName() : "Project");
						loadConsts(cfile, suite != null ? suite.getParamTable() : tproj.getSysVars());
					}
					else
					{
						LogUtil.trace("Canot Load consts from {} to {}", dir + "/" + fname, suite != null ? suite.getName() : "Project");
					}
					continue;
				}
				
				//TSuite define
				if (line.matches(TS_REGEX))
				{
					Matcher m = Pattern.compile(TS_REGEX).matcher(line);
					m.find();
					suite = TSuite.createV2(StringUtil.trim(m.group(2)), Integer.valueOf(m.group(1)));
					suite.setProject(tproj);
					continue;
				}
				
				if (line.indexOf(".fitc") > -1 /*&& "rasthl".indexOf(line.charAt(0)) > -1*/)
				{
					if (suite == null)
					{
						suite = TSuite.createV2(runfile.getName().split("\\.")[0], 0);
						suite.setProject(tproj);
					}
					
					List<String> parts = new ArrayList<>();
					StringUtil.readCFParamStr(line + ")", 0, parts, ' ');
					String tcfStr = findTcFile(parts);
					File tcf = tproj.getProjectFile(tcfStr);
					if (tcf == null) throw new DataInvalidException("Fit Case file not exist: " + tcfStr);
					
					loadScripts(tcf, suite, tproj);
					config(parts, suite.getLast());
				}
			}
		}
		catch(DataInvalidException de)
		{
			LogUtil.error("DataInvalidException {}@{} : {}", runfile.getName(), lineTag, line);
			throw de;
		}
		catch (Exception e) {
			LogUtil.error("Line parse {}:{}, {}@{} : {}", e.getClass().getName(), e.getMessage(), runfile.getName(), lineTag, line);
		}
	}
	
	private static String findTcFile(List<String> parts){
		for (String s : parts)
		{
			if (s.endsWith(".fitc")) return s;
		}
		
		return null;
	}
	
	public static void config(List<String> cfgs, TestCase tcase) throws DataInvalidException, ParamIncertitudeException
	{
		if (tcase == null) throw new DataInvalidException("TCase must not null!");
		
		boolean args = false;
		for (String cfg : cfgs)
		{
			if ("run".equals(cfg))
			{
				continue;
			}
			else if ("async".equals(cfg))
			{
				tcase.getParamTable().add("level_wait", String.valueOf(tcase.getOwner().getLevel()));
				continue;
			}
			else if (cfg.endsWith(".fitc"))
			{
				args = true;
				continue;
			}
			else if (args)
			{
				cfg = StringUtil.trim(cfg);
				int idx = cfg.indexOf('=');
				String name = cfg.substring(0, idx).trim();
				String value = cfg.substring(idx + 1).trim();
				tcase.addParameter(name, value);
				continue;
			}
			
			if (cfg.matches(Consts.FUNC_PATTERRN))
			{
				Matcher m = Pattern.compile(Consts.FUNC_PATTERRN).matcher(cfg);
				m.find();
				String func = m.group(1);
				String fbody = m.group(2);
				
				switch (func) {
				case "async":
					String lvStr = fbody.trim();
					if (lvStr.isEmpty())
					{
						tcase.getParamTable().add("level_wait", String.valueOf(tcase.getOwner().getLevel()));
					}
					else
					{
						int wl = Integer.valueOf(lvStr);
						String lv = wl < 0 || wl - 1 < 0 ? String.valueOf(Integer.MAX_VALUE) : String.valueOf(wl - 1);
						tcase.getParamTable().add("level_wait", lv);
					}
					break;
				case "schedule":
					tcase.setSchedule(Schedule.parse(fbody.trim(), tcase.getParamTable()));
					break;
				case "heartbeat":
					fbody = fbody.trim();
					if (fbody.matches("^[0-9]{3,}[\\s]*,[\\s*](true|false)$"))
					{
						tcase.addParameter("heartbeat", fbody.trim());
					}
					else
						throw new DataInvalidException("HeartBeat set invalid.");
					break;
				case "loop":
					tcase.setLoopVars(trim(fbody));
					break;
				case "thread":
					tcase.setThread(Integer.valueOf(trim(expend(fbody, tcase.getParamTable()))));
					break;
				default:
					LogUtil.error("Unkown modifier: {}", cfg);
					throw new DataInvalidException("Unkown modifier: " + func);
				}
			}// FuncCall modifier
			else
			throw new DataInvalidException("Unkown modifier2: " + cfg);
		}
	}
	
	public static void loadScripts(File sf, TProject tproj) throws DataInvalidException
	{
		TSuite suite = TSuite.create(sf.getName());
		suite.setProject(tproj);
		
		loadScripts(sf, suite, tproj);
	}
	
	public static void loadScripts(File sf, TSuite suite, TProject tproj) throws DataInvalidException
	{
		try {
			ScriptReader sr = new ScriptReader() {
				BufferedReader br = new BufferedReader(new FileReader(sf));
				public String readLine() throws Exception {
					return br.readLine();
				}
			};
			loadScripts(sr, suite, tproj);
		} catch (FileNotFoundException e) {
			//ignore
		}
	}
	
	public static void loadScripts(final List<String> scripts, TSuite suite, TProject tproj) throws DataInvalidException
	{
		ScriptReader sr = new ScriptReader() {
			int idx = 0;
			public String readLine() throws Exception {
				return idx < scripts.size() ? scripts.get(idx++) : null;
			}
		};
		loadScripts(sr, suite, tproj);
	}
	
	public static void loadScripts(ScriptReader sr, TSuite suite, TProject tproj) throws DataInvalidException
	{
		int lineTag = 0;
		String line = null;
		try {
			
			TestCase tcase = null;
			TestStep step = null;
			
			boolean comment = false;
			while((line = sr.readLine()) != null)
			{
				lineTag++;
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
			LogUtil.error("DataInvalidException {}@{} : {}", de.getMessage(), lineTag, line);
			throw de;
		}
		catch (Exception e) {
			LogUtil.error("Line parse {}:{} @{} : {}", e.getClass().getName(), e.getMessage(), lineTag, line);
		}
		
		TestCase tcase = suite.getLast();
		if (tcase != null)
		{
			try {
				tcase.valid();
				tcase.initTStage();
			} catch (DataInvalidException e) {
				//invalid test case
				suite.getTestCases().remove(tcase);
			}
		}
		
	}//End of loadScripts
	
	private static void parseParaLine(String line, TestCase tcase, TestStep step, int lineTag) throws DataInvalidException, ParamIncertitudeException
	{
		int idx = -1;
		if ((idx = line.indexOf('=')) > 0)
		{
			String name = trim(line.substring(0, idx));
			String value = line.substring(idx + 1).trim();
			if ("title".equals(name) && step == null)
			{
				if (tcase == null) throw new DataInvalidException("Cannot set testcase property in AW scripts.");
				tcase.setName(value);
				return;
			}
			else if ("tid".equals(name) && step == null)
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
		case "g_out":
		case "out": 
			step.addOut(fbody.trim(), true);
			break;
		case "l_out":
			step.addOut(fbody.trim(), false);
			break;
		case "chk": 
			String [] pars = readFuncParam(fbody);
			if (pars.length < 3)
			{
				pars = readFuncParam(fbody, ' ');
			}
			
			if (pars.length >= 3)
			{
				String ff[] = new String[pars.length - 3];
				for (int i = 0; i < ff.length; i++)
				{
					ff[i] = pars[i + 3];
				}
				step.getVfTable().add(pars[0].trim(), pars[1].trim(), pars[2].trim(), ff);
			}
			else
			{
				throw new DataInvalidException("Invalid CheckPoint @line:" + lineTag);
			}
			break;
		case "waitfor":
			String [] tpars = fbody.split(",");
			if (tpars.length <= 3)
			{
				try {
					step.setWaitfor(Integer.valueOf(trim(expend(tpars[0], step.getParamTable()))));
					step.setInternal(tpars.length > 1 ? Integer.valueOf(trim(expend(tpars[1], step.getParamTable()))) : 3000);
					step.setLoopCondition(tpars.length == 3 ? tpars[2].trim() : null);
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
				step.setDelay(Integer.valueOf(trim(expend(fbody, step.getParamTable()))));
			} catch (NumberFormatException e) {
				throw new DataInvalidException("Invalid delay @line:" + lineTag);
			}
			break;
		case "loop":
			if (step != null)
			{
				step.setLoopVars(trim(fbody));
			}
			else
			{
				if (tcase != null)
					tcase.setLoopVars(trim(fbody));
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		case "skip":
			if (step != null)
			{
				step.setSkipExpr(trim(fbody));
			}
			else
			{
				if (tcase != null)
					tcase.setSkipExpr(trim(fbody));
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		case "thread":
			if (step != null)
			{
				step.setThread(Integer.valueOf(trim(expend(fbody, tcase.getParamTable()))));
			}
			else
			{
				if (tcase != null)
					tcase.setThread(Integer.valueOf(trim(expend(fbody, tcase.getParamTable()))));
				else
					throw new DataInvalidException("Cannot set testcase property in AW scripts.");
			}
			break;
		case "async":// support test case and test step
		{
			if (step != null)
			{
				step.setAsync(true);
			}
			else
			{
				String lvStr = trim(expend(fbody, tcase.getParamTable()));
				if (lvStr.isEmpty())
				{
					tcase.getParamTable().add("level_wait", String.valueOf(tcase.getOwner().getLevel()));
				}
				else
				{
					int wl = Integer.valueOf(lvStr);
					String lv = wl < 0 || wl - 1 < 0 ? String.valueOf(Integer.MAX_VALUE) : String.valueOf(wl - 1);
					tcase.getParamTable().add("level_wait", lv);
				}
			}
			break;
		}
		case "schedule":
		{
			if (step != null)
			{
				step.setSchedule(Schedule.parse(fbody.trim(), step.getParamTable()));
			}
			else
			{
				tcase.setSchedule(Schedule.parse(fbody.trim(), tcase.getParamTable()));
			}
			break;
		}
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
	
	public static void loadConsts(File constsFile, ParameterTable table)
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
					String name = trim(line.substring(0, idx));
					name = name.startsWith("sys.para.") ? name.substring(9) : name;
					table.add(name, line.substring(idx + 1).trim());
				}
			}
		} catch (Exception e) {
			LogUtil.error("Load consts fail: {}", e.getMessage());
		}
	}//End of loadConsts()
	
	
	static final String DEF_PATTERN = "^def[ ]{1,}([a-zA-Z0-9_]{1,128})\\(([\\S\\s]*)\\)$";
	public static void loadAWScripts(File awFile)
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
					String params[] = readFuncParam(paramstr);
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
