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

import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.model.CheckPoint;
import org.fastj.fit.model.Consts;
import org.fastj.fit.tool.StringUtil;

public class INPara extends ChkPara{

	private List<String> exps = new ArrayList<String>();
	
	public INPara(String name, String expValue) {
		super(expValue, "in", expValue);
		splits(exps, expValue);
	}

	@Override
	public CheckPoint check() {
		CheckPoint cp = new CheckPoint();
		boolean eq = exps.contains(this.realValue);
		cp.setResultCode(eq ? Consts.PASS : Consts.FAIL);
		cp.setMessages(String.format("[%s] IN [%s] : %s", realValue, expValue, cp.isPass() ? "PASS" : "FAIL"));
		return cp;
	}
	
	public ChkPara copy(String rp)
	{
		INPara ip = new INPara(rp != null ? rp : rpath, expValue);
		return ip;
	}

	public static void splits(List<String> l, String v)
	{
		String vstr = v.startsWith("[") ? v.substring(1) : v;
		vstr = vstr.endsWith("]") ? vstr.substring(0, vstr.length() - 1) : vstr;
		
		try {
			String[] paras = StringUtil.readFuncParam(v);
			for(String p : paras)
			{
				l.add(p);
			}
		} catch (DataInvalidException e) {
		}
	}
}
