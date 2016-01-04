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
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TCNode;
import org.fastj.fit.intf.TResult;
import org.fastj.fit.log.NodeLogger;

/**
 * TestCase
 * 
 * @author zhouqingquan
 *
 */
public class TestCase implements TCNode{
	private String tid;
	private String desc;
	private String name = "New TestCase";
	private TSuite owner;
	private TProject project;
	
	private int thread = 0;
	
	/**
	 * switch expression, if (switchExpr != null && !JS.val(switchExpr)) off
	 */
	private String skipExpr = null;
	
	/**
	 * TestStep can expends itself by loopVars<br>
	 * loopVar:  var1=@data:value1,value2,value3
	 */
	private String loopVars = null;
	
	private ParameterTable paramTable = new ParameterTable();
	private List<TestStep> steps = new ArrayList<TestStep>();
	
	private int result = Consts.PASS;
	
	private long startTime;
	private long endTime;
	
	private NodeLogger loggor = new NodeLogger();
	
	private List<TResult> results = new ArrayList<TResult>();
	
	/**
	 * schedule(total, numPerloop, interval)
	 */
	private Schedule schedule = null;

	public void valid() throws DataInvalidException
	{
		if (isLevelWait() && getWaitLevel() < getOwner().getLevel())
		{
			throw new DataInvalidException("Wait on level setting invalid.");
		}
	}
	
	public void addStep(TestStep aw)
	{
		steps.add(aw);
	}
	
	public TestCase addParameter(String pname, String pvalue)
	{
		paramTable.add(pname, pvalue);
		return this;
	}
	
	public void mergeResult(TResult tr)
	{
		if (tr.getResult() != Consts.PASS && result == Consts.PASS)
		{
			result = tr.getResult();
		}
		results.add(tr);
	}
	
	public boolean isLevelWait()
	{
		return paramTable.lcontains("level_wait");
	}
	
	public int getWaitLevel()
	{
		return paramTable.getInt("level_wait", -1);
	}
	
	public void append(NodeLogger log)
	{
		loggor.append(log);
	}
	
	public TestCase append(String log)
	{
		loggor.append(log);
		return this;
	}
	
	public NodeLogger getNLoggor()
	{
		return loggor;
	}
	
	@Override
	public String getLog() {
		return loggor.getLog();
	}
	
	public int getResult() {
		return result;
	}
	
	public void cleanResult() {
		this.result = PASS;
		results.clear();
	}

	public List<TResult> getResults()
	{
		return results;
	}

	public List<TestStep> getSteps() {
		return steps;
	}
	
	public void initTStage()
	{
		boolean td = false;
		for (TestStep step : steps)
		{
			if (step.getTestStage() == Consts.TSTAGE_TEARDOWN)
			{
				td = true;
				continue;
			}
			if (td)
			{
				step.setTestStage(Consts.TSTAGE_TEARDOWN);
			}
		}
		
		int len = steps.size();
		boolean bl = false;
		for (int i = len - 1; i >= 0; i--)
		{
			TestStep step = steps.get(i);
			if (step.getTestStage() == Consts.TSTAGE_PRE)
			{
				bl = true;
				continue;
			}
			if (bl)
			{
				step.setTestStage(Consts.TSTAGE_PRE);
			}
		}
	}
	
	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ParameterTable getParamTable() {
		return paramTable;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public TProject getProject() {
		return project;
	}

	public void setProject(TProject project) {
		this.project = project;
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		this.thread = thread;
	}
	
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public int threadNum(int data)
	{
		if (thread > 0) return thread > data ? data : thread;
		
		return data;
	}
	
	public long getStartTime()
	{
		return startTime;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public String getSkipExpr() {
		return skipExpr;
	}

	public void setSkipExpr(String skipExpr) {
		this.skipExpr = skipExpr;
	}

	public String getLoopVars() {
		return loopVars;
	}

	public void setLoopVars(String loopVars) {
		this.loopVars = loopVars;
	}

	public TSuite getOwner() {
		return owner;
	}
	
	public org.fastj.fit.intf.TSuite getSuite()
	{
		return owner;
	}

	public void setOwner(TSuite owner) {
		this.owner = owner;
		owner.addTestCase(this);
		paramTable.setParent(owner.getParamTable());
	}

}
