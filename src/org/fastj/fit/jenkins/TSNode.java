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

package org.fastj.fit.jenkins;

public class TSNode {
	
	private int runCnt = 0;
	private int failureCnt = 0;
	private int errorCnt = 0;
	private int skipCnt;
	
	private long start = 0;
	private long end = 0;
	
	private String name;
	
	public long getStartTime()
	{
		return start;
	}
	
	public String getName()
	{
		return name;
	}
	
	public long getRunTime()
	{
		return end - start;
	}
	
    public int runCount()
    {
    	return runCnt;
    }
    
    public int failureCount()
    {
    	return failureCnt;
    }
    
    public int errorCount()
    {
    	return errorCnt;
    }
    
    public int skipCount()
    {
    	return skipCnt;
    }
    
    public void setRunCnt(int runCnt) {
		this.runCnt = runCnt;
	}

	public void setFailureCnt(int failureCnt) {
		this.failureCnt = failureCnt;
	}

	public void setErrorCnt(int errorCnt) {
		this.errorCnt = errorCnt;
	}

	public void setSkipCnt(int skipCnt) {
		this.skipCnt = skipCnt;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setName(String name) {
		this.name = name;
	}
}
