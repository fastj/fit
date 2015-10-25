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
 * @command json_get(${json_var}, json_path)
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
		if (args == null || args.length != 2)
		{
			throw new DataInvalidException("Func[json_get]: 1 or 2 args");
		}
		
		String jsonCt = StringUtil.expend(args[0], ptable);
		String path = StringUtil.expend(args[1], ptable);
		
		Object ov = JSONHelper.jsonValue(path, JSONHelper.getJson(jsonCt));
		return ov == null || ov instanceof String ? String.valueOf(ov) : JSONHelper.jsonString(ov);
	}

}
