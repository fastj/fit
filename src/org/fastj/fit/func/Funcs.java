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

package org.fastj.fit.func;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.tool.StringUtil;

/**
 * IFunction注册管理
 * 
 * @author zhouqingquan
 *
 */
public class Funcs {
	
	private static final Map<String, IFunction> FUNCS = new HashMap<String, IFunction>();
	
	public static void regist(IFunction func)
	{
		FUNCS.put(func.name(), func);
		LogUtil.trace("Load Func [{}]", func.name());
	}
	
	public static String runFunc(String fname, List<String> args, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		IFunction func = FUNCS.get(fname);
		
		if (func == null)
		{
			throw new ParamIncertitudeException("Func [" + fname + "] not registed.");
		}
		
		return func.frun(table, args.toArray(new String[args.size()]));
	}
	
	public static String runFunc(String fname, String argstr, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		List<String> params = new ArrayList<String>();
		int len = StringUtil.readCFParamStr(argstr + ")", 0, params);
		
		if (len != argstr.length() + 1) throw new DataInvalidException("Read Break: " + argstr);
		
		return runFunc(fname, params, table);
	}
	
	static
	{
		regist(new IFFunc());
		regist(new GetDate());
		regist(new JSonGet());
		regist(new PatternGet());
		regist(new ReadFile());
		regist(new LoopDataGenerator());
		regist(new JSFunc());
		regist(new VarCheckFunc());
		
	}
}
