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

package org.fastj.fit.tool;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展目录加载
 * 
 * @author zhouqingquan
 *
 */
public class PCLoader {
	
	private static ClassLoader gLoader = initCL("plugins", PCLoader.class.getClassLoader());
	
	public static ClassLoader getgLoader() {
		return gLoader;
	}

	public static Class<?> load(String clazz)
	{
		try {
			return Class.forName(clazz, true, gLoader);
		} catch (ClassNotFoundException e) {
		}
		
		return null;
	}
	
	public static ClassLoader initCL(String dir, ClassLoader parent)
	{
		File ext = new File(dir);
		List<URL> urls = new ArrayList<>();
		walkDir(ext, urls);
		return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
	}
	
	private static void walkDir(File dir, List<URL> urls)
	{
		if (dir.isDirectory())
		{
			File ds[] = dir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory();
				}
			});
			
			if (ds != null)
			{
				for (File df : ds)
				{
					walkDir(df, urls);
				}
			}
			
			File fs[] = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			if (fs != null)
			{
				for (File jf : fs)
				{
					try {
						urls.add(jf.toURI().toURL());
					} catch (MalformedURLException e) {
					}
				}
			}
		}
	}
}
