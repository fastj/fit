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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.fcall.CallUtil;
import org.fastj.fit.func.Funcs;
import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.HttpSignFactory;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.IHttpSigner;
import org.fastj.fit.intf.PerfStat;
import org.fastj.fit.intf.PostProc;
import org.fastj.fit.model.TProject;
import org.fastj.fit.tool.StringUtil;

public class Plugin {
	
	private String plugJar;
	
	private String plugContent;
	
	private List<String> intfs = new ArrayList<>();
	
	private List<URL> depends = new ArrayList<>();
	
	private URLClassLoader loader = null;
	
	public Plugin(String jar, String content) throws DataInvalidException
	{
		this.plugJar = jar;
		this.plugContent = content;
		init();
	}
	
	public void load(TProject tproj) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException
	{
		for (String intf : intfs)
		{
			String keyStr = null;
			if (intf.contains(" "))
			{
				String parts[] = intf.split(" "); 
				intf = parts[0].trim();
				keyStr = parts[1].trim();
			}
			
			Class<?> c = Class.forName(intf, true, loader);
			Object iobj = null;
			if (StringUtil.isValidVar(keyStr))
			{
				Constructor<?> cc = c.getConstructor(String.class);
				iobj = cc.newInstance(keyStr);
			}
			else
			{
				iobj = c.newInstance();
			}
			
			if (iobj instanceof PostProc)
			{
				if (tproj != null){
					tproj.setPostProc((PostProc) iobj);
				}
			}
			else if (iobj instanceof IFuncCall)
			{
				CallUtil.regist((IFuncCall) iobj);
			}
			else if (iobj instanceof IFunction)
			{
				Funcs.regist((IFunction) iobj);
			}
			else if (iobj instanceof PerfStat)
			{
				if (tproj != null){
					tproj.setPerfStat((PerfStat) iobj);
				}
			}
			else if (iobj instanceof IHttpSigner)
			{
				HttpSignFactory.regist((IHttpSigner) iobj);
			}
		}
	}
	
	public String toString()
	{
		return "FitPlug: " + plugJar;
	}
	
	private void init() throws DataInvalidException
	{
		if (plugContent == null || plugContent.trim().isEmpty()) return;
		
		try {
			depends.add(new URL("jar:injar:" + plugJar + "!/"));
		} catch (MalformedURLException e) {
			throw new DataInvalidException("Invalid url: jar:injar:" + plugJar + "!/");
		}
		
		for (String line  : plugContent.split("\n"))
		{
			line = line.trim();
			if (line.isEmpty()) continue;
			if (line.startsWith("intf")){
				String intf = line.substring(4).trim();
				intfs.add(intf);
			}
			else if (line.startsWith("lib"))
			{
				String dep = line.substring(3).trim();
				try {
					depends.add(new URL("jar:injar:" + dep + "!/"));
				} catch (MalformedURLException e) {
					throw new DataInvalidException("Invalid url: jar:injar:" + dep + "!/");
				}
			}
		}
		loader = new URLClassLoader(depends.toArray(new URL[depends.size()]), this.getClass().getClassLoader());
	}
}
