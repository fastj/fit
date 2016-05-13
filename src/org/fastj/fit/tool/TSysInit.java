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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.TProject;
import org.fastj.fit.tool.ld.InjarRes;
import org.fastj.fit.tool.ld.InjarURLStreamHandlerFactory;
import org.fastj.fit.tool.ld.Plugin;

public class TSysInit {
	
	public static ClassLoader fitExtLoader = null;
	
	private static final InjarURLStreamHandlerFactory INJARFACTORY = new InjarURLStreamHandlerFactory(TSysInit.class.getClassLoader());
	private static final String EXT_NAME = "lib";
	private static final String COMMON_ROOT = "/extlib";
	private static final String COMMON_PREFIX = "extlib/";
	private static final String PLUGINS_ROOT = "/plugins";
	private static final String PLUGINS_PREFIX = "plugins/";
	private static boolean init = false;
	private static HashMap<String, Plugin> plugins = new HashMap<>();
	
	public static void init() throws DataInvalidException
	{
		synchronized (TSysInit.class) {
			if (init) return;
			init = true;
		}
		
		URL.setURLStreamHandlerFactory(INJARFACTORY);
		changeCL();
		walkInJars(COMMON_ROOT, COMMON_PREFIX, false);
		walkInJars(PLUGINS_ROOT, PLUGINS_PREFIX, true);
	}
	
	public static InputStream getResourceStream(String name)
	{
		InjarRes res = INJARFACTORY.getResource(name);
		if (res == null) return null;
		return res.openStream();
	}
	
	public static void addResource(String name, InjarRes ins)
	{
		INJARFACTORY.addRes(name, ins);
	}
	
	public static void loadPlugins(TProject tproj) throws DataInvalidException
	{
		init();
		
		for (Plugin plug : plugins.values())
		{
			try {
				plug.load(tproj);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
				throw new DataInvalidException(e);
			}
		}
	}
	
	private static void changeCL()
	{
		try {
			ClassLoader curr = Thread.currentThread().getContextClassLoader();
			
			if (curr == null) {
				LogUtil.error("Load fit ext classloader fail: no ctx classloader");
				return;
			}
			
			ClassLoader currParent = curr.getParent();
			fitExtLoader = initBaseCL(EXT_NAME, currParent);
			
			Field pf = ClassLoader.class.getDeclaredField("parent");
			pf.setAccessible(true);
			pf.set(curr, fitExtLoader);
		} catch (Throwable e) {
			LogUtil.error("Load ext lib fail: {}", e.getMessage());
		}
	}
	
	private static ClassLoader initBaseCL(String dir, ClassLoader parent)
	{
		File ext = new File(dir);
		List<URL> urls = new ArrayList<>();
		walkDir(ext, urls);
		walkJarExt(urls);
		return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
	}
	
	private static void walkInJars(String root, String prefix, boolean plug)
	{
		URL common = TSysInit.class.getResource(root);
		if (common == null) return;
		try {
			JarURLConnection jurl = (JarURLConnection) common.openConnection();
			JarFile jf = jurl.getJarFile();
			Enumeration<JarEntry> jes = jf.entries();
			while ( jes.hasMoreElements())
			{
				JarEntry je = jes.nextElement();
				String name = je.getName();
				if (name.startsWith(prefix) && name.endsWith(".jar") && !je.isDirectory())
				{
					InjarRes res = new InjarRes();
					res.baseJar = jf;
					res.entry = name;
					addResource(name, res);
					if (plug){
						parsePlugin(TSysInit.class.getResource("/" + name), name);
					}
				}
			}
		} catch (Throwable e) {
			LogUtil.error("Load fit jars fail(0): {}", e.getMessage());
		}
	}
	
	private static void parsePlugin(URL jarurl, String plugJar)
	{
		if (jarurl != null)
		{
			JarInputStream jins = null;
			InputStream jin = null;
			try {
				JarURLConnection jurl = (JarURLConnection) jarurl.openConnection();
				jin = jurl.getInputStream();
				jins = new JarInputStream(jin, true);
				JarEntry je = jins.getNextJarEntry();
				while ((je = jins.getNextJarEntry()) != null)
				{
					if (!je.getName().equals("fit.plugin")) continue;
					byte[] buff = new byte[(int) je.getSize()];
					jins.read(buff);
					
					Plugin plugin = new Plugin(plugJar, new String(buff, "utf-8"));
					plugins.put(plugJar, plugin);
				}
			} catch (Throwable e) {
				LogUtil.error("Parse plugin from {} fail.", e, plugJar);
			}
			finally
			{
				if (jins != null)
				{
					try {
						jins.close();
					} catch (IOException e) {
						LogUtil.error("Close jar input fail: e={}", e.getMessage());
					}
				}
				if (jin != null)
				{
					try {
						jin.close();
					} catch (IOException e) {
						LogUtil.error("Close jar input fail: e={}", e.getMessage());
					}
				}
			}
		}
		
	}
	
	private static void walkJarExt(List<URL> urls)
	{
		URL ext = TSysInit.class.getResource("/" + EXT_NAME);
		if (ext == null) return;
		try {
			JarURLConnection jurl = (JarURLConnection) ext.openConnection();
			JarFile jf = jurl.getJarFile();
			Enumeration<JarEntry> jes = jf.entries();
			while ( jes.hasMoreElements())
			{
				JarEntry je = jes.nextElement();
				String name = je.getName();
				if (name.startsWith(EXT_NAME + "/") && name.endsWith(".jar") && !je.isDirectory())
				{
					urls.add(new URL("jar:injar:" + name + "!/"));
					InjarRes res = new InjarRes();
					res.baseJar = jf;
					res.entry = name;
					addResource(name, res);
				}
			}
		} catch (Throwable e) {
			LogUtil.error("Load fit jars fail: e={}:{}", e.getClass(), e.getMessage());
		}
	}
	
	private static void walkDir(File dir, List<URL> urls)
	{
		if (dir.exists() && dir.isDirectory())
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
						LogUtil.error("WalkDir fail: file={}, e={}", jf.getName(), e.getMessage());
					}
				}
			}
		}
	}
	
}
