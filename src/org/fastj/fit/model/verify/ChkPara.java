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

public abstract class ChkPara {
	
	private String key;
	protected String rpath;
	protected String expValue;
	protected String opkey;
	protected String realValue;
	
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
	
	public abstract CheckPoint check();
	
	public abstract ChkPara copy(String rpath);
	
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
	
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String || obj instanceof ChkPara)
		{
			return obj.hashCode() == hashCode();
		}
		return false;
	}
}
