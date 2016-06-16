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

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;

/**
 * Adaptor to other auto test script (log)
 * 
 * @author zhouqingquan
 *
 */
public class TRSetLogCall implements IFuncCall{

	@Override
	public String name() {
		return "setlog";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		ctx.put("__log__", ctx.getLog().getLog());
		return new FuncResponse();
	}

}
