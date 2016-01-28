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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

/**
 * 正则取值
 * 
 * @command pt_get(regex, [group: 0,] string_value)
 * 
 * @author zhouqingquan
 */
public class PatternGet implements IFunction{

	@Override
	public String name() {
		return "pt_get";
	}

	@Override
	public String frun(ParameterTable table, String... args) throws ParamIncertitudeException, DataInvalidException {
		
		int g = 0;
		String regex;
		String str;
		if (args.length == 2)
		{
			regex = StringUtil.expend(args[0], table);
			str = StringUtil.expend(args[1], table);
		}
		else if (args.length == 3)
		{
			regex = StringUtil.expend(args[0], table);
			g = Integer.valueOf(StringUtil.expend(args[1], table));
			str = StringUtil.expend(args[2], table);
		}
		else
		{
			throw new DataInvalidException("Func[pt_get]: 2 or 3 parameters. args.length=" + args.length);
		}
		
		Matcher m = Pattern.compile(regex).matcher(str);
		if (m.find())
		{
			return m.group(g);
		}
		
		return "null";
	}

}
