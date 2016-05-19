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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.fastj.fit.log.LogUtil;
import org.fastj.fit.log.NodeLogger;

/**
 * 用例执行上下文
 * 
 * @author zhouqingquan
 *
 */
public class TContext {
	
	private TProject project = null;
	private TResult result = null;
	private TCNode testCase = null;
	private NodeLogger log = null;
	private ParameterTable privOut = new ParameterTable();
	private Map<String, Object> context = new HashMap<String, Object>();
	private List<AWNode> asyncNodes = new ArrayList<>();
	private List<ScheduleTask> stasks = new ArrayList<>();
	
	public TProject getProject() {
		return project;
	}

	public void setProject(TProject project) {
		this.project = project;
	}

	public TCNode getTestCase() {
		return testCase;
	}

	public void setTestCase(TCNode testCase) {
		this.testCase = testCase;
	}

	public TResult getResult() {
		return result;
	}

	public void setResult(TResult result) {
		this.result = result;
	}

	public NodeLogger getLog() {
		return log;
	}

	public void setLog(NodeLogger log) {
		this.log = log;
	}

	public void put(String key, Object value)
	{
		synchronized (context) {
			Object obj = context.remove(key);
			if (obj != null && obj instanceof ICloseable) {
				String id = "removed-" + System.currentTimeMillis();
				context.put(id, obj);
			}
			context.put(key, value);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key)
	{
		synchronized (context) {
			return (T) context.get(key);
		}
	}
	
	public boolean contains(String key)
	{
		synchronized (context) {
			return context.containsKey(key);
		}
	}
	
	public void closeResource(String id)
	{
		Object ref = null;
		synchronized (context) {
			ref = context.remove(id);
		}
		if (ref != null && ref instanceof ICloseable)
		{
			try {
				((ICloseable) ref).close();
			} catch (Throwable e) {
				LogUtil.error("Auto close resource fail", e);
			}
		}
	}
	
	public void closeResources()
	{
		synchronized (context) {
			Iterator<Entry<String, Object>> iter = context.entrySet().iterator();
			while (iter.hasNext()) {
				Object obj = iter.next().getValue();
				if (obj instanceof ICloseable) {
					iter.remove();
					try {
						((ICloseable) obj).close();
					} catch (Throwable e) {
						LogUtil.error("Auto close resource fail", e);
					}
				}
			}
		}
	}

	public ParameterTable getOuts() {
		return privOut;
	}
	
	public void out(String pname, String pvalue)
	{
		privOut.add(pname, pvalue);
	}
	
	public void addAsync(CountDownLatch cdl, ExecutorService executor, Runnable bcall, boolean shutdown)
	{
		asyncNodes.add(new AWNode(cdl, executor, bcall, shutdown));
	}
	
	public void addSchedule(ScheduleTask task)
	{
		synchronized (stasks) {
			stasks.add(task);
		}
	}
	
	public void rmvSchedule(ScheduleTask task)
	{
		synchronized (stasks) {
			stasks.remove(task);
		}
	}
	
	public void waitComplete()
	{
		while(true){
			synchronized (stasks) {
				if (stasks.isEmpty()) {
					break;
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		
		for (AWNode wn : asyncNodes) {
			wn.await();
		}
		asyncNodes.clear();
	}
	
	private static class AWNode{
		
		private CountDownLatch cdl;
		private ExecutorService executor;
		private Runnable callBack = null;
		private boolean shutdownE = false;
		
		AWNode(CountDownLatch cdl, ExecutorService executor, Runnable bcall, boolean shutdown)
		{
			this.cdl = cdl;
			this.executor = executor;
			callBack = bcall;
			shutdownE = shutdown;
		}
		
		public void await()
		{
			try {
				cdl.await(); 
			} catch (Throwable e) {
				LogUtil.error("TestStep async wait interrupted", e);
			}
			finally{
				if (shutdownE) {
					executor.shutdown();
				}
				callBack.run();
			}
		}
	}
}
