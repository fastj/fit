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

package org.fastj.fit.model.verify;

import org.fastj.fit.model.CheckPoint;
import org.fastj.fit.model.Consts;
import org.fastj.fit.tool.JS;

public class DefaultPara extends ChkPara{

	public DefaultPara(String name, String op, String expValue) {
		super(name, op, expValue);
	}

	@Override
	public CheckPoint check() {
		//=, !=, >, >=, <, <=
		CheckPoint cp = new CheckPoint();
		boolean isNum = expValue.matches(Consts.NUMBER_REGEX) && realValue.matches(Consts.NUMBER_REGEX);
		String op = opkey;
		op = "=".equals(op) ? "==" : op;
		boolean rlt = true;
		
		if (isNum)
		{
			rlt = JS.compare(realValue, op, expValue);
		}
		else
		{
			if ("==".equals(op))
			{
				rlt = realValue.equals(expValue);
			}
			else if ("!=".equals(op))
			{
				rlt = !realValue.equals(expValue);
			}
			else
			{
				rlt = false;
			}
		}
		
		cp.setResultCode(rlt ? Consts.PASS : Consts.FAIL);
		cp.setMessages(String.format("[%s] %s [%s] : %s", realValue, opkey, expValue, cp.isPass() ? "PASS" : "FAIL"));
		return cp;
	}

	@Override
	public ChkPara copy(String rp) {
		DefaultPara dp = new DefaultPara(rp != null ? rp : rpath, opkey, expValue);
		return dp;
	}

}
