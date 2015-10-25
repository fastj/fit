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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

/**
 * @command date([string_pattern], [delta long(ms): 0])
 * 
 * @author zhouqingquan
 *
 */
public class GetDate implements IFunction{

	@Override
	public String name() {
		return "date";
	}

	@Override
	public String frun(ParameterTable table, String... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length == 0)
		{
			return String.valueOf(System.currentTimeMillis());
		}
		
		long delta = 0;
		String pt = null;
		
		args[0] = StringUtil.trim(StringUtil.expend(args[0], table));
		if (args[0].matches("[0-9-]{1,}"))
		{
			if (args.length != 1) throw new DataInvalidException("Func[date] args invalid: more args " + args.length);
			delta = Long.valueOf(args[0]);
			return String.valueOf(System.currentTimeMillis() + delta);
		}
		
		pt = args[0];
		if (args.length == 2)
		{
			try {
				delta = Long.valueOf(StringUtil.trim(StringUtil.expend(args[1], table)));
			} catch (NumberFormatException e) {
				throw new DataInvalidException("Func[date] args invalid: Not Number");
			}
		}
		else if (args.length > 2)
		{
			throw new DataInvalidException("Func[date] args invalid: more args " + args.length);
		}
		
		Date d = new Date(System.currentTimeMillis() + delta);
		SimpleDateFormat sdf = new SimpleDateFormat(pt, Locale.ENGLISH);
		return sdf.format(d);
	}

}
