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

/**
 * 参数
 * 
 * @author zhouqingquan
 *
 */
public class Parameter {
	
	private final String name;
	private String value;
	private String desc;
	
	public Parameter(String name)
	{
		if (name == null || name.trim().isEmpty())
		{
			throw new IllegalArgumentException("Parameter name is null.");
		}
		
		this.name = name;
	}
	
	public static Parameter by(String name)
	{
		return new Parameter(name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public Parameter setValue(String value) {
		this.value = value == null ? "null" : value;
		return this;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public Parameter setDesc(String desc) {
		this.desc = desc == null ? "" : desc;
		return this;
	}
	
}
