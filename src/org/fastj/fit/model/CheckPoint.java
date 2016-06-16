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

import org.fastj.fit.intf.TCNode;

public class CheckPoint {
	private int resultCode = TCNode.PASS;
	private String messages;
	
	public boolean isPass()
	{
		return resultCode == TCNode.PASS;
	}
	
	public String statusString() {
		switch (resultCode) {
		case TCNode.PASS:
			return "PASS";
		case TCNode.FAST_FAIL:
			return "FASTFAIL";
		default:
			return "FAIL";
		}
	}

	public boolean isFastFail()
	{
		return resultCode == TCNode.FAST_FAIL;
	}
	
	public boolean isBlock()
	{
		return resultCode == TCNode.BLOCKED;
	}
	
	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getMessages() {
		return messages;
	}

	public void setMessages(String messages) {
		this.messages = messages;
	}

	public String toString()
	{
		return resultCode + "  " + messages;
	}
}
