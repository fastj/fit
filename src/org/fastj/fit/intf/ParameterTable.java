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

package org.fastj.fit.intf;

import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.log.LogUtil;

/**
 * 参数列表
 * 
 * 变量的优先级： 子表 > 父表
 * 
 * @author zhouqingquan
 *
 */
public final class ParameterTable {
	
	private ParameterTable parent = null;

	private List<Parameter> table = new ArrayList<Parameter>();
	
	/**
	 * 上级变量表
	 * 
	 * @return ParameterTable
	 */
	public ParameterTable getParent() {
		return parent;
	}

	public void setParent(ParameterTable parent) {
		this.parent = parent;
	}
	
	public boolean isEmpty()
	{
		return table.isEmpty();
	}
	
	public List<Parameter> gets()
	{
		return table;
	}
	
	public synchronized ParameterTable add(String pname, String pvalue, String desc)
	{
		Parameter p = get(pname, false);
		if (p != null)  //update value
		{
			p.setValue(pvalue).setDesc(desc);
		}
		else
		{
			p = Parameter.by(pname);
			p.setValue(pvalue).setDesc(desc);
			table.add(p);
		}
		return this;
	}
	
	public synchronized ParameterTable add(String pname, String pvalue)
	{
		Parameter p = get(pname, false);
		if (p != null)
		{
			//update value
			p.setValue(pvalue);
		}
		else
		{
			p = Parameter.by(pname);
			p.setValue(pvalue);
			table.add(p);
		}
		
		return this;
	}
	
	public boolean lcontains(String name)
	{
		return get(name, false) != null;
	}
	
	public boolean gcontains(String name)
	{
		return get(name) != null;
	}
	
	/**
	 * 查询变量， 当前列表优先级高于父表优先级
	 * @param name  变量名
	 * @return {@link Parameter}
	 */
	public Parameter get(String name)
	{
		return get(name, true);
	}
	
	/**
	 * 查找变量
	 * 
	 * @param name    变量名
	 * @param walkup  是否查找父表
	 * @return {@link Parameter}
	 */
	public Parameter get(String name, boolean walkup)
	{
		if (name == null) return null;
		
		for (Parameter p : table)
		{
			if (p == null)
			{
				LogUtil.warn("ERROR list p= null: " + table) ;
			}
			if (p != null && p.getName().equals(name))
			{
				return p;
			}
		}
		
		if (walkup && parent != null)
		{
			return parent.get(name, walkup);
		}
		
		return null;
	}
	
	public String getPara(String name, String def)
	{
		Parameter p = get(name);
		return p == null ? def : p.getValue();
	}
	
	public int getInt(String name, int def)
	{
		Parameter p = get(name);
		try {
			return p == null ? def : Integer.valueOf(p.getValue());
		} catch (NumberFormatException e) {
			return def;
		}
	}
	
	public ParameterTable copy()
	{
		ParameterTable ptable = new ParameterTable();
		ptable.setParent(parent);
		
		for (Parameter p : table)
		{
			ptable.add(p.getName(), p.getValue());
		}
		
		return ptable;
	}
	
	/**
	 * merge parameters from other table
	 * @param adds Parameters
	 */
	public void addAll(final ParameterTable adds)
	{
		for (final Parameter p : adds.table)
		{
			add(p.getName(), p.getValue());
		}
	}
	
}
