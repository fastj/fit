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
import java.util.Map;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.model.CheckPoint;
import org.fastj.fit.tool.JSONHelper;
import org.fastj.fit.tool.StringUtil;

public class VerifyTable {
	
	private List<ChkPara> table = new ArrayList<ChkPara>();
	
	public VerifyTable add(String name, String op, String expValue) throws DataInvalidException
	{
		return add(name, op, expValue, new String[0]);
	}
	
	public VerifyTable add(String name, String op, String expValue, String[] fastfails) throws DataInvalidException
	{
		if (!StringUtil.isValidVar(name) || !StringUtil.isValidVar(expValue))
		{
			throw new DataInvalidException("Verify Param name/value is null or empty!");
		}
		
		ChkPara cp = ChkParaFactory.get(name, op, expValue);
		cp.setFastFails(fastfails);
		table.add(cp);
		return this;
	}
	
	public List<ChkPara> getChkPara()
	{
		return table;
	}
	
	public StepResult check()
	{
		StepResult sr = new StepResult();
		if (table.isEmpty()) {
			sr.addMessage("No CheckList.");
			return sr;
		}
		
		for (ChkPara p : table)
		{
			CheckPoint cp = p.check();
			sr.addMessage(cp.getMessages());
			sr.setResult(cp.getResultCode());
		}
		
		return sr;
	}
	
	public void fillValue(Map<String, Object> jo, ParameterTable ptable) throws DataInvalidException, ParamIncertitudeException
	{
		for (int idx = table.size() - 1; idx >= 0; idx--)
		{
			ChkPara cp = table.get(idx);
			cp.expends(ptable);
			Object v = null;
			String path = StringUtil.expend(cp.getPath(), ptable);
			if (path.startsWith("json."))
			{
				v = JSONHelper.jsonValue(path, jo);
				if (path.contains("[loop()]"))
				{
					String rpath = path;
					if (v == null || "nil".equals(v) || !(v instanceof List<?>))
					{
						cp.setRealValue("nil");
					}
					else
					{
						List<?> vl = (List<?>) v;
						table.remove(idx);
						for (int i = 0 ;i < vl.size(); i++)
						{
							ChkPara ncp = cp.copy(rpath.replace("loop()", String.valueOf(i)));
							ncp.setRealValue(String.valueOf(vl.get(i)));
							table.add(ncp);
						}
					}
				}
				else if (path.contains("[find()]"))
				{
					if (v == null || "nil".equals(v) || !(v instanceof List<?>))
					{
						cp.setRealValue("nil");
					}else{
						List<?> vl = (List<?>) v;
						for (int i = 0 ;i < vl.size(); i++)
						{
							cp.setRealValue(String.valueOf(vl.get(i)));
							if (cp.check().isPass()) break;
						}
					}
				}
				else
				{
					String pv = v == null || v instanceof String ? String.valueOf(v) : JSONHelper.jsonString(v);
					cp.setRealValue(pv);
				}
				
				continue;
			}
			else if (path.startsWith("xpath."))
			{
				v = "Not Support Xpath";
			}
			else 
			{
				v = path;
			}
			
			cp.setRealValue(String.valueOf(v));
		}
	}
	
	public VerifyTable copy()
	{
		VerifyTable vt = new VerifyTable();
		for (ChkPara cp : table)
		{
			vt.table.add(cp.copy(null));
		}
		
		return vt;
	}
}
