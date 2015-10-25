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
import java.util.List;

/**
 * 测试用例结果
 * 
 * @author zhouqingquan
 *
 */
public class TResult{
	
	private String name;
	private String tid;
	private int result = 0;
	private List<StepResult> results = new ArrayList<StepResult>();

	public void mergeResult(StepResult rlt)
	{
		results.add(rlt);
		result = result < rlt.getResult() ? rlt.getResult() : result;
	}
	
	public void mergeResult(List<StepResult> rlts)
	{
		for (StepResult rlt : rlts)
		{
			mergeResult(rlt);
		}
	}
	
	public String getResultDesc()
	{
		return result == TCNode.PASS ? "OK" : result == TCNode.BLOCKED ? "BLOCKED" : "FAIL";
	}
	
	public int getResult() {
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}
	
	public String toString()
	{
		StringBuilder buff = new StringBuilder();
		for (StepResult sr : results)
		{
			buff.append("\r\n>>>>>> ").append("STEP takes ").append(sr.getCost()).append(" sec.");
			buff.append(" RLT=").append(sr.isPass() ? "OK" : sr.isBlock() ? "BLOCK" : "FAIL").append("\r\n");
			buff.append("REQ = ").append(sr.getRequest()).append("\r\n");
			buff.append("RESP = ").append(sr.getResponse()).append("\r\n");
			for (String msg : sr.getMessages())
			{
				buff.append(msg).append("\r\n");
			}
		}
		
		return buff.toString();
	}
}
