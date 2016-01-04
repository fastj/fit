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
import org.fastj.fit.tool.JS;
import org.fastj.fit.tool.StringUtil;

/**
 * @command if(expr, cmd1[, cmd2])
 * 
 * @author zhouqingquan
 *
 */
public class IFFunc implements IFunction{

	@Override
	public String name() {
		return "if";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args.length < 2 || args.length > 3)
		{
			throw new DataInvalidException("Func[if] needs 2 or 3 args.");
		}
		
		String expr = StringUtil.expend(args[0], ptable);
		boolean ifrlt = JS.val(expr);
		
		if (ifrlt)
		{
			return StringUtil.expend(args[1], ptable);
		}
		else
		{
			if (args.length == 3)
			{
				return StringUtil.expend(args[2], ptable);
			}
		}
		
		return null;
	}

}
