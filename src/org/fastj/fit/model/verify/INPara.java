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
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.model.CheckPoint;
import org.fastj.fit.model.Consts;
import org.fastj.fit.tool.StringUtil;

public class INPara extends ChkPara{

	private List<String> exps = new ArrayList<String>();
	private List<String> ffail = new ArrayList<>();
	
	public INPara(String name, String expValue) throws DataInvalidException {
		super(expValue, "in", expValue);
		splits(exps, expValue);
	}
	
	public INPara(INPara in) {
		super(in.rpath, "in", in.expValue);
		this.fastFails = in.fastFails;
		this.exps.addAll(in.exps);
		this.ffail.addAll(in.ffail);
	}

	@Override
	public CheckPoint check() {
		CheckPoint cp = new CheckPoint();
		boolean eq = exps.contains(this.realValue);
		boolean ff = ffail.contains(this.realValue);
		cp.setResultCode(eq ? Consts.PASS : ff ? Consts.FAST_FAIL : Consts.FAIL);
		cp.setMessages(String.format("[%s] IN [%s] : %s", realValue, ff ? ffail : expValue, cp.statusString()));
		return cp;
	}
	
	public ChkPara copy(String rp)
	{
		INPara ip = new INPara(this);
		if (rp != null) {
			ip.rpath = rp;
		}
		return ip;
	}

	@Override
	public void setFastFails(String[] fastFails) throws DataInvalidException {
		super.setFastFails(fastFails);
		for (String s : fastFails)
		{
			splits(ffail, s);
		}
	}
	
	@Override
	public void expends(ParameterTable ptable) throws ParamIncertitudeException, DataInvalidException {
		List<String> uexps = new ArrayList<String>();
		List<String> uffail = new ArrayList<>();
		for (String expv : exps)
		{
			uexps.add(StringUtil.expend(expv, ptable));
		}
		for (String ffv : ffail)
		{
			uffail.add(StringUtil.expend(ffv, ptable));
		}
		this.exps = uexps;
		this.ffail = uffail;
	}
	
	public static void splits(List<String> l, String v) throws DataInvalidException
	{
		String vstr = v.startsWith("[") ? v.substring(1) : v;
		vstr = vstr.endsWith("]") ? vstr.substring(0, vstr.length() - 1) : vstr;
		
		String[] paras = StringUtil.readFuncParam(v);
		for (String p : paras) {
			l.add(p);
		}
	}
}
