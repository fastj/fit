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

import java.util.HashMap;
import java.util.Map;

/**
 * 用例执行上下文
 * 
 * @author zhouqingquan
 *
 */
public class TContext {
	
	private Map<String, Object> context = new HashMap<String, Object>();
	
	public void put(String key, Object value)
	{
		context.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		return (T) context.get(key);
	}
	
	public boolean contains(String key)
	{
		return context.containsKey(key);
	}
}
