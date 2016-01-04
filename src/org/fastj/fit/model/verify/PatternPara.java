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

public class PatternPara extends ChkPara{

	public PatternPara(String rpath, String op, String expValue) {
		super(rpath, op, expValue);
	}

	@Override
	public CheckPoint check() {
		CheckPoint cp = new CheckPoint();
		boolean eq = this.realValue.matches(this.expValue);
		
		boolean ff = false;
		if (!eq && fastFails != null && fastFails.length > 0)
		{
			for (String ffStr : fastFails)
			{
				ff = this.realValue.matches(ffStr);
				if (ff) break;
			}
		}
		
		cp.setResultCode(eq ? Consts.PASS : ff ? Consts.FAST_FAIL : Consts.FAIL);
		cp.setMessages(String.format("[%s] %s [%s] : %s", realValue, opkey, expValue, cp.isPass() ? "PASS" : "FAIL"));
		return cp;
	}
	
	public ChkPara copy(String rp)
	{
		PatternPara pp = new PatternPara(rp != null ? rp : rpath, opkey, expValue);
		return pp;
	}
	
}
