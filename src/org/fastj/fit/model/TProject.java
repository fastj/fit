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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.PerfStat;
import org.fastj.fit.intf.PostProc;
import org.fastj.fit.intf.ScheduleTask;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.tool.DefaultPerfStat;

/**
 * Test Project
 * 
 * @author zhouqingquan
 *
 */
public class TProject implements org.fastj.fit.intf.TProject{

	private String consts = CONSTS_FILE;
	
	private String dir;
	private ParameterTable sysVars = new ParameterTable();
	private List<TSuite> suites = new ArrayList<TSuite>();
	private Map<Integer, List<WOLNode>> levelWaitMap = new HashMap<Integer, List<WOLNode>>();
	private PostProc postProc = null;
	private PerfStat perfStat = new DefaultPerfStat();
	
	private ReadWriteLock hblock = new ReentrantReadWriteLock();
	
	public void waitOnLevel(int level, CountDownLatch cdl, ExecutorService executor, TestCase tcase, ScheduleTask st)
	{
		synchronized (levelWaitMap) {
			int lv = level;
			List<WOLNode> wl = levelWaitMap.get(lv);
			if (wl == null)
			{
				wl = new ArrayList<WOLNode>();
				levelWaitMap.put(lv, wl);
			}
			
			wl.add(new WOLNode(cdl, executor,tcase, st));
		}
	}
	
	public void levelWait(int level)
	{
		List<WOLNode> wl = levelWaitMap.get(level);
		if (wl == null || wl.isEmpty())
		{
			return;
		}
		
		//TODO while ?
		while(!wl.isEmpty())
		for (int i = wl.size() - 1; i >= 0; i--)
		{
			WOLNode cdl = wl.get(i);
			cdl.await(); 
			wl.remove(i);
			if (postProc != null)
			{
				try {
					postProc.finish(cdl.tcase);
				} catch (Throwable e) {
					LogUtil.error("Call postProc[finishTC] fail: ", e);
				}
			}
		}
	}
	
	public int getMaxLevel()
	{
		int mxl = 0;
		for (TSuite ts : suites)
		{
			mxl = ts.getLevel() > mxl ? ts.getLevel() : mxl;
		}
		
		return mxl;
	}
	
	public List<TSuite> getSuites(int level)
	{
		List<TSuite> rlt = new ArrayList<TSuite>();
		for (TSuite ts : suites)
		{
			if (ts.getLevel() == level)
			{
				rlt.add(ts);
			}
		}
		return rlt;
	}
	
	public File getProjectFile(String file)
	{
		if (file == null) return null;
		File d = new File(dir);
		File res = new File(d, file);
		return res.exists() ? res : null;
	}
	
	public List<File> getProjectFiles()
	{
		File df = new File(dir);
		List<File> list = new ArrayList<File>();
		findScripts(df, list, TS_NAME_REGEX);
		return list;
	}
	
	public List<File> getAWscripts()
	{
		File df = new File(dir);
		List<File> list = new ArrayList<File>();
		findScripts(df, list, AW_NAME_REGEX);
		return list;
	}
	
	public List<File> getTestCaseFiles()
	{
		File df = new File(dir);
		List<File> list = new ArrayList<File>();
		findScripts(df, list, TC_NAME_REGEX);
		return list;
	}
	
	public List<File> getRunFiles()
	{
		File df = new File(dir);
		List<File> list = new ArrayList<File>();
		findScripts(df, list, TR_NAME_REGEX);
		return list;
	}
	
	private void findScripts(File dir, List<File> sl, final String regex)
	{
		File []dfl = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		
		if (dfl != null && dfl.length != 0)
		{
			for (File subdir : dfl)
			{
				findScripts(subdir, sl, regex);
			}
		}
		
		File [] scripts = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(regex);
			}
		});
		
		if (scripts != null)
		{
			for (File f : scripts)
			{
				sl.add(f);
			}
		}
	}
	
	public String getResourcePath(String fileName)
	{
		File resDir = new File(dir, RESOURCES_DIR);
		File res = new File(resDir, fileName);
		return res.getAbsolutePath();
	}
	
	public File getResourceFile(String fileName)
	{
		File resDir = new File(dir, RESOURCES_DIR);
		File res = new File(resDir, fileName);
		return res;
	}
	
	public File getLogFile(String fileName)
	{
		File resDir = new File(dir, LOG_DIR);
		resDir.mkdirs();
		File res = new File(resDir, fileName);
		return res;
	}
	
	public File getDataFile(String fileName)
	{
		File resDir = new File(dir, RUNDATA_DIR);
		resDir.mkdirs();
		File res = new File(resDir, fileName);
		return res;
	}
	
	public String getDir() {
		return dir;
	}
	
	public void setDir(String dir)
	{
		this.dir = dir;
	}

	public ParameterTable getSysVars() {
		return sysVars;
	}

	public List<TSuite> getSuites() {
		return suites;
	}
	
	public void addSuite(TSuite suite)
	{
		if (suite == null)
		{
			return;
		}
		if (!suites.contains(suite))
		{
			suites.add(suite);
		}
	}

	public File getConsts() {
		return new File(dir, consts);
	}
	
	public File getSetUp()
	{
		return new File(dir, SETUP_FILE);
	}

	public void setConsts(String consts) {
		this.consts = consts;
	}
	
	public PostProc getPostProc() {
		return postProc;
	}

	public void setPostProc(PostProc postProc) {
		this.postProc = postProc;
	}
	
	public void start()
	{
		if (postProc != null)
		{
			postProc.start(this);
		}
	}

	public ReadWriteLock getHblock() {
		return hblock;
	}

	public PerfStat getPerfStat() {
		return perfStat;
	}

	public void setPerfStat(PerfStat perfStat) {
		this.perfStat = perfStat;
	}

	private static class WOLNode{
		
		private CountDownLatch cdl;
		private ExecutorService executor;
		private TestCase tcase;
		private ScheduleTask schedule;
		
		WOLNode(CountDownLatch cdl, ExecutorService executor, TestCase tcase, ScheduleTask sch)
		{
			this.cdl = cdl;
			this.executor = executor;
			this.tcase = tcase;
			this.schedule = sch;
		}
		
		public void await()
		{
			while (schedule != null && !schedule.completed())
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			
			try {
				cdl.await(); 
			} catch (Throwable e) {
				LogUtil.error("TestCase level wait interrupted", e);
			}
			finally{
				if (schedule != null) executor.shutdown();
			}
		}
	}
	
}
