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
import static org.fastj.fit.tool.StringUtil.*;

/**
 * ldata(prefix [, start: 1], len)
 * 
 * @author zhouqingquan
 *
 */
public class LoopDataGenerator implements IFunction{

	@Override
	public String name() {
		return "ldata";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 1 || args.length > 3)
		{
			throw new DataInvalidException("Func[ldata] needs 2 or 3 args.");
		}
		
		String prefix = args.length > 1 ? expend(args[0], ptable) : "";
		int start = 1;
		int len = 0;
		try {
			start = args.length <= 2 ? 1 : Integer.parseInt(expend(args[1], ptable));
			len = Integer.parseInt(expend(args.length == 2 ? args[1] : args.length == 3 ? args[2] : args[0], ptable));
		} catch (NumberFormatException e) {
			throw new DataInvalidException("Func[ldata] NumberArgs invalid.");
		}
		
		len = len < 0 ? 0 : len;
		start = start < 0 ? 0 : start;
		
		StringBuilder buff = new StringBuilder((prefix.length() + 5) * len);
		buff.append("@data:");
		for (int i = 0; i < len; i++)
		{
			buff.append(prefix).append(start + i).append(", ");
		}
		
		if (len > 0)
		{
			buff.deleteCharAt(buff.length() - 1);
			buff.deleteCharAt(buff.length() - 1);
		}
		
		return buff.toString();
	}

}
