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
import org.fastj.fit.intf.StepResult;
import org.fastj.fit.model.verify.VerifyTable;

/**
 * 测试动作、步骤
 * 测试参数可被循环展开执行
 * 
 * @author zhouqingquan
 *
 */
public class TestStep{
	private String name;
	private TestCase owner;
	private ParameterTable paramTable = new ParameterTable();
	
	private List<TOut> outCmdLines = new ArrayList<TOut>();
	
	private List<StepResult> results = new ArrayList<StepResult>();
	
	private int result = Consts.PASS;
	
	private int testStage = Consts.TSTAGE_MAIN;
	
	/**
	 * Verify table
	 */
	private VerifyTable vfTable = new VerifyTable();
	
	/**
	 * Function command,like a java function invoke:
	 * <li>$func(para1,para2,${para3VarName})
	 */
	private String funcCmd = null;
	
	/**
	 * sleep after run
	 */
	private int delay = 200;
	
	/**
	 * TestStep can expends itself by loopVars<br>
	 * loopVar:  var1=@data:value1,value2,value3
	 */
	private String loopVars = null;
	
	/**
	 * Retry function, break when pass or timeout
	 */
	private long waitfor = 0;
	
	/**
	 * Sleep time while wait for special result
	 */
	private long internal = 0;
	
	/**
	 * Function execute timeout
	 */
	private int timeout = 60000;
	
	/**
	 * switch expression, if (skipExpr != null && JS.val(skipExpr)) off
	 */
	private String skipExpr = null;
	
	/**
	 * threads number
	 */
	private int thread = 0;
	
	/**
	 * schedule(total, numPerloop, interval)
	 */
	private Schedule schedule = null;
	
	/**
	 * Run model: sync, async
	 */
	private boolean async = false;
	
	/**
	 * while(result != pass && result != fastfail && loopCondition && not timeout) loop;
	 */
	private String loopCondition = null;
	
	public TestStep addParameter(String pname, String pvalue)
	{
		paramTable.add(pname, pvalue);
		return this;
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

	public VerifyTable getVfTable() {
		return vfTable;
	}

	public void setVfTable(VerifyTable vfTable) {
		this.vfTable = vfTable;
	}

	public TestCase getOwner() {
		return owner;
	}

	public void setOwner(TestCase owner) {
		this.owner = owner;
		paramTable.setParent(owner.getParamTable());
	}

	public String getFuncCmd() {
		return funcCmd;
	}

	public void setFuncCmd(String funcCmd) {
		this.funcCmd = funcCmd;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int sleep) {
		this.delay = sleep;
	}

	public String getLoopVars() {
		return loopVars;
	}

	public void setLoopVars(String loopVars) {
		this.loopVars = loopVars;
	}

	public long getWaitfor() {
		return waitfor;
	}

	public void setWaitfor(long waitfor) {
		this.waitfor = waitfor;
	}

	public long getInternal() {
		return internal;
	}

	public void setInternal(long internal) {
		this.internal = internal;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public List<TOut> getOutCmdLines() {
		return outCmdLines;
	}
	
	public void addOut(String line, boolean g) throws DataInvalidException
	{
		String [] par = line.split("," , 2);
		if (par.length == 1) {
			throw new DataInvalidException("Invalid out expr: " + line);
		}
		
		TOut to = new TOut(par[0].trim(), par[1].trim(), g);
		outCmdLines.add(to);
	}
	
	public String getSkipExpr() {
		return skipExpr;
	}

	public void setSkipExpr(String skipExpr) {
		this.skipExpr = skipExpr;
	}

	public int getThread() {
		return thread;
	}
	
	public int threadNum(int data)
	{
		if (thread > 0) return thread > data ? data : thread;
		
		return data;
	}

	public void setThread(int thread) {
		this.thread = thread;
	}
	
	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public synchronized void mergeSResult(StepResult srlt)
	{
		results.add(srlt);
		result = result < srlt.getResult() ? srlt.getResult() : result;
	}

	public List<StepResult> getResults() {
		return results;
	}

	public int getResult() {
		return result;
	}

	public int getTestStage() {
		return testStage;
	}

	public void setTestStage(int testStage) {
		this.testStage = testStage;
	}
	
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public String getLoopCondition() {
		return loopCondition;
	}

	public void setLoopCondition(String loopCondition) {
		this.loopCondition = loopCondition;
	}

	public TestStep copy()
	{
		TestStep copy = new TestStep();
		copy.delay = delay;
		copy.funcCmd = funcCmd;
		copy.internal = internal;
		copy.loopVars = loopVars;
		copy.name = name;
		copy.outCmdLines = outCmdLines;
		copy.owner = owner;
		copy.paramTable = paramTable.copy();
		copy.result = result;
		copy.skipExpr = skipExpr;
		copy.testStage = testStage;
		copy.thread = thread;
		copy.timeout = timeout;
		copy.vfTable = vfTable.copy();
		copy.waitfor = waitfor;
		return copy;
	}
	
}
