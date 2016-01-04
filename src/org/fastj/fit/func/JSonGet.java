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

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.JSONHelper;
import org.fastj.fit.tool.StringUtil;

/**
 * @command json_get([${json_var: _resp_}, ]json_path)
 * 
 * @author zhouqingquan
 *
 */
public class JSonGet implements IFunction{

	@Override
	public String name() {
		return "json_get";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		if (args == null || args.length < 1)
		{
			throw new DataInvalidException("Func[json_get]: need 2 args");
		}
		
		String jsonCt = StringUtil.expend(args.length > 1 ? args[0] : "${_resp_}", ptable);
		String path = StringUtil.expend(args[args.length == 1 ? 0 : 1], ptable);
		if (path.startsWith("json.")) path = path.substring(5);
		Object ov = JSONHelper.jsonValue(path, JSONHelper.getJson(jsonCt));
		return str(ov);
	}
	
	private static final String str(Object v)
	{
		if (v == null) return "null";
		if (v instanceof String || v.getClass().isPrimitive()) return String.valueOf(v);
		return JSONHelper.jsonString(v);
	}

}
