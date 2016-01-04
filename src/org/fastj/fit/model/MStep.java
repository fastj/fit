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

package org.fastj.fit.model;

import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.Parameter;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

/**
 * AW
 * 
 * @author zhouqingquan
 *
 */
public class MStep 
{
	private String name;
	private List<String> params = new ArrayList<>();
	private List<String> must = new ArrayList<>();
	private List<Parameter> optParas = new ArrayList<>();
	private List<TestStep> steps = new ArrayList<>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TestStep> getSteps() {
		return steps;
	}
	
	public void addStep(TestStep tstep)
	{
		steps.add(tstep);
	}

	public void addParam(String para) throws DataInvalidException
	{
		if (!para.contains("=") && !optParas.isEmpty()) throw new DataInvalidException("Mandatory parameter must before opts.");
		params.add(para);
		if (!para.contains("="))
		{
			must.add(para);
		}
		else
		{
			String par[] = para.split("=", 2);
			optParas.add(new Parameter(StringUtil.trim(par[0])).setValue(par[1]));
		}
	}
	
	public String getParamName(int i) throws DataInvalidException
	{
		if (i >= params.size()) throw new DataInvalidException("AW def invalid or call invalid.");
		return i < must.size() ? must.get(i) : optParas.get(i - must.size()).getName();
	}
	
	public int mSize()
	{
		return must.size();
	}
	
	public void check(ParameterTable table) throws ParamIncertitudeException
	{
		for (String mkey : must)
		{
			if (!table.lcontains(mkey)) throw new ParamIncertitudeException(mkey + " is not provided");
		}
		
		for (Parameter p : optParas)
		{
			if (!table.lcontains(p.getName()) && !"None".equals(p.getValue()))
			{
				table.add(p.getName(), p.getValue());
			}
		}
	}
	
	public boolean isMustProvide(int i) throws DataInvalidException
	{
		return i < must.size();
	}
	
	public String getDefValue(int i)  throws DataInvalidException
	{
		if (i >= params.size() || i < must.size()) throw new DataInvalidException("AW def invalid or call invalid.");
		String str = optParas.get(i - must.size()).getValue();
		return str;
	}
	
	public List<String> getParams() {
		return params;
	}
}
