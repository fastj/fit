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

package org.fastj.fit.fcall;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.model.Consts;
import org.fastj.fit.model.MStep;
import org.fastj.fit.model.TestStep;

/**
 * 内置函数注册管理
 * 
 * @author zhouqingquan
 *
 */
public class CallUtil {
	
	private static final Map<String, IFuncCall> FUNCS = new HashMap<String, IFuncCall>();
	private static final Map<String, MStep> AWS = new HashMap<String, MStep>();
	
	public static void regist(IFuncCall func)
	{
		FUNCS.put(func.name(), func);
	}
	
	public static void registAW(MStep aw)
	{
		AWS.put(aw.getName(), aw);
	}
	
	public static MStep getMStep(String funcCmd)
	{
		Matcher m = Pattern.compile(Consts.FUNC_PATTERRN).matcher(funcCmd);
		m.find();
		String func = m.group(1);
		
		return AWS.get(func);
	}
	
	public static boolean isMStep(String funcCmd) throws ParamIncertitudeException
	{
		Matcher m = Pattern.compile(Consts.FUNC_PATTERRN).matcher(funcCmd);
		m.find();
		String func = m.group(1);
		IFuncCall ifunc = FUNCS.get(func);
		if (ifunc != null)
		{
			return false;
		}
		
		if (!AWS.containsKey(func)) throw new ParamIncertitudeException("AW " + func + " is not registed.");
		
		return true;
	}
	
	/**
	 * Run FunctionCall (Only FCall)
	 * 
	 * @param step
	 * @param ctx
	 * @param table
	 * @return FuncResponse
	 * @throws ParamIncertitudeException
	 * @throws DataInvalidException
	 */
	public static FuncResponse run(TestStep step, TContext ctx, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		String fcmd = step.getFuncCmd();
		
		Matcher m = Pattern.compile(Consts.FUNC_PATTERRN).matcher(fcmd);
		m.find();
		String func = m.group(1);
		String paras = m.group(2);
		
		IFuncCall ifunc = FUNCS.get(func);
		if (ifunc != null)
		{
			FuncResponse fr = ifunc.run(ctx, table, paras.trim());
			if (fr == null) throw new DataInvalidException("Func[" + func + "] is invalid.");
			return fr;
		}
		
		throw new ParamIncertitudeException("Func " + func + " is not registed.");
	}
	
	public static IFuncCall getFCall(String name)
	{
		return FUNCS.get(name);
	}
	
	static
	{
		//default
		regist(new DBFunc());
		regist(new NOPFunc());
		regist(new TRCall());
		regist(new TRSetLogCall());
		regist(new Delay());
		regist(new EchoCall());
	}
	
}
