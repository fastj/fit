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
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.model.Consts;
import org.fastj.fit.model.verify.VerifyTable;
import org.fastj.fit.tool.JSONHelper;

import static org.fastj.fit.tool.StringUtil.*;

public class ChkFunc implements IFunction{

	@Override
	public String name() {
		return "chk";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || (args.length != 1 && args.length != 3)) throw new DataInvalidException("Func[chk] requires 3 parameters.");
		
		if (args.length == 1)
		{
			args = readFuncParam(args[0], ' ');
		}
		
		if (args.length != 3) throw new DataInvalidException("Func[chk] requires 3 parameters.");
		
		VerifyTable vt = new VerifyTable();
		vt.add(args[0], args[1], args[2]);
		
		vt.fillValue(JSONHelper.getJson(expendVar("_resp_", ptable)), ptable);
		StepResult sr = vt.check();
		return String.valueOf(sr.getResult() == Consts.PASS);
	}

}
