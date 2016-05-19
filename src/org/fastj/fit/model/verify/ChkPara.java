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

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.model.CheckPoint;
import org.fastj.fit.tool.StringUtil;

public abstract class ChkPara {
	
	private String key;
	protected String rpath;
	protected String expValue;
	protected String opkey;
	protected String realValue;
	protected String[] fastFails = new String[0];
	
	public ChkPara(String rpath, String op, String expValue)
	{
		this.rpath = rpath;
		this.expValue = expValue;
		this.opkey = op;
		this.key = rpath + ":" + expValue;
	}
	
	public String getPath() {
		return rpath;
	}
	
	public String getOpKey(){
		return opkey;
	}
	
	public abstract CheckPoint check();
	
	public abstract ChkPara copy(String rpath);
	
	public void expends(ParameterTable ptable) throws ParamIncertitudeException, DataInvalidException
	{
		this.expValue = StringUtil.expend(this.expValue, ptable);
		for (int i = 0; i < fastFails.length; i++)
		{
			fastFails[i] = StringUtil.expend(fastFails[i], ptable);
		}
	}
	
	public void setRealValue(String rv)
	{
		realValue = rv;
	}

	public String getKey() {
		return key;
	}
	
	public String getExpValue() {
		return expValue;
	}

	public String getRealValue() {
		return realValue;
	}
	
	public String[] getFastFails() {
		return fastFails;
	}

	public void setFastFails(String[] fastFails) throws DataInvalidException {
		this.fastFails = fastFails;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ChkPara)
		{
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	public String toString(){
		return String.format("%s %s %s", rpath, opkey, expValue);
	}
}
