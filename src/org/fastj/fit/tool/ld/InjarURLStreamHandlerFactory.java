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

package org.fastj.fit.tool.ld;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;

public class InjarURLStreamHandlerFactory implements URLStreamHandlerFactory {
	private HashMap<String, InjarRes> resoures = new HashMap<>();

	public InjarURLStreamHandlerFactory(ClassLoader cl) {
	}

	public URLStreamHandler createURLStreamHandler(String protocol) {
		if ("injar".equals(protocol))
		{
			return new InjarUrlStreamHander();
		}
		return null;
	}
	
	public void addRes(String name, InjarRes ins)
	{
		resoures.put(name, ins);
	}
	
	public InjarRes getResource(String name)
	{
		return resoures.get(name);
	}
}
