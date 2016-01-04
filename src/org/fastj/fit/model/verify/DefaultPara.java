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
		boolean rlt = check(expValue);
		
		boolean ff = false;
		if ( !rlt && fastFails != null && fastFails.length > 0)
		{
			for (String ffStr : fastFails)
			{
				if (ff = check(ffStr)) break;
			}
		}
		cp.setResultCode(rlt ? Consts.PASS : ff ? Consts.FAST_FAIL : Consts.FAIL);
		cp.setMessages(String.format("[%s] %s [%s] : %s", realValue, opkey, expValue, cp.isPass() ? "PASS" : "FAIL"));
		return cp;
	}
	
	private boolean check(String v)
	{
		boolean isNum = v.matches(Consts.NUMBER_REGEX) && realValue.matches(Consts.NUMBER_REGEX);
		String op = opkey;
		op = "=".equals(op) ? "==" : op;
		boolean rlt = true;
		
		if (isNum)
		{
			rlt = JS.compare(realValue, op, v);
		}
		else
		{
			if ("==".equals(op))
			{
				rlt = realValue.equals(v);
			}
			else if ("!=".equals(op))
			{
				rlt = !realValue.equals(v);
			}
			else
			{
				rlt = false;
			}
		}
		
		return rlt;
	}

	@Override
	public ChkPara copy(String rp) {
		DefaultPara dp = new DefaultPara(rp != null ? rp : rpath, opkey, expValue);
		return dp;
	}

}
