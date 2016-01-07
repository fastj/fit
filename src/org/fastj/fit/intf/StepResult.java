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
 * TestStep Result
 * 
 * @author zhouqingquan
 *
 */
public class StepResult{
	
	private String request = null;
	private String response = null;
	private long start = 0;
	private int cost = 0;
	private List<String> messages = new ArrayList<String>();
	private ParameterTable loopData = new ParameterTable();
	private int code = TCNode.PASS;

	public boolean isBlock() {
		return TCNode.BLOCKED == code;
	}

	public boolean isPass() {
		return code == TCNode.PASS;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> results) {
		if (results == null || results.isEmpty()) return;
		this.messages.addAll(results);
	}
	
	public void addMessage(String msg)
	{
		this.messages.add(msg);
	}

	public boolean isFastFail() {
		return code == TCNode.FAST_FAIL;
	}

	public void setFastFail() {
		this.code = TCNode.FAST_FAIL;
	}

	public int getResult() {
		return code;
	}

	public void setResult(int code) {
		this.code = code > this.code ? code : this.code;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public ParameterTable getLoopData() {
		return loopData;
	}

}
