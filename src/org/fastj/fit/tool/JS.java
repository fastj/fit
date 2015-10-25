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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.Consts;

/**
 * JS compare & val
 * 
 * @author zhouqingquan
 *
 */
public class JS {
	static ScriptEngineManager manager = new ScriptEngineManager();
	static ScriptEngine engine = manager.getEngineByName("javascript");
	
	public static boolean compare(String real, String op, String exp)
	{
		//may be null
		real = String.valueOf(real);
		
		//handle '
		real = real.replaceAll("'", "\\\\'");
		exp = exp.replaceAll("'", "\\\\'");
		
		if (real.matches(Consts.NUMBER_REGEX) && exp.matches(Consts.NUMBER_REGEX))
		{
			Object o = val(String.format("%s %s %s", real, op, exp));
			return Boolean.valueOf(String.valueOf(o));
		}
		
		if ("==".equals(op) || "!=".equals(op))
		{
			Object o = val(String.format("'%s' %s '%s'", real, op, exp));
			return Boolean.valueOf(String.valueOf(o));
		}
		
		LogUtil.error("String compare not support: {}", op);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T val(String cmd)
	{
		try {
			Object o = engine.eval(cmd);
			return (T) o;
		} catch (ScriptException e) {
			LogUtil.error("JSRun cmd fail: {}", e, cmd);
			return null;
		}
	}
	
}
