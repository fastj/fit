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

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.tool.StringUtil;

/**
 * @command $nop()
 * 
 * @author zhouqingquan
 *
 */
public class NOPFunc implements IFuncCall{

	@Override
	public String name() {
		return "nop";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		FuncResponse fr = new FuncResponse();
		fr.setCode(0);
		fr.setEntity(new HashMap<String, Object>());
		fr.setRequest("NOP: " + StringUtil.expend(argStr, table));
		fr.setPhrase("");
		return fr;
	}

}
