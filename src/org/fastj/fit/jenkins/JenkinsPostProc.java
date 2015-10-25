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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastj.fit.intf.PostProc;
import org.fastj.fit.intf.TCNode;
import org.fastj.fit.intf.TProject;
import org.fastj.fit.intf.TSuite;
import org.fastj.fit.log.LogUtil;

/**
 * Log&Result for Jenkins
 * 
 * @author zhouqingquan
 *
 */
public class JenkinsPostProc implements PostProc {

	TProject tproj;
	long start;
	long end;
	
	List<TCNode> tcDList = new ArrayList<>();
	Map<String, XMLJUnitResult> emap = new HashMap<>();
	
	@Override
	public void end() {
		end = System.currentTimeMillis();
		
		for (XMLJUnitResult xjr : emap.values())
		{
			xjr.endTestSuite();
		}
		
		FileOutputStream glog = null;
		try {
			glog = new FileOutputStream(tproj.getLogFile("fit.log"));
			glog.write(LogUtil.getLog());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		finally
		{
			if (glog != null)
			{
				try {
					glog.close();
				} catch (IOException e) {
				}
			}
		}
		
		System.out.println("<=== End test at " + new Date());
	}

	@Override
	public void start(TProject tproj) {
		this.tproj = tproj;
		start = System.currentTimeMillis();
		System.out.println("===> Start test at " + new Date());
	}

	@Override
	public void finish(TCNode tcn) {
		tcDList.add(tcn);
		//ExtPoint: Set Result to 3rd Systems
		//ExtPoint: Write TestCase Log to 3rd Systems
		
		String tsname = tcn.getSuite().getName();
		if (!emap.containsKey(tsname))
		{
			emap.put(tsname, new XMLJUnitResult());
			emap.get(tsname).startTestSuite(tproj, tcn.getSuite(), tcn.getStartTime());
		}
		
		emap.get(tsname).appendNode(tcn);
		
		TSNode tsn = emap.get(tsname).node;
		tsn.setEnd(tcn.getEndTime());
		tsn.setRunCnt(tsn.runCount() + 1);
		
		if (tcn.getResult() != TCNode.PASS)
		{
			tsn.setFailureCnt(tsn.failureCount() + 1);
		}
	}

	@Override
	public void finish(TSuite suite) {
		//Can write log when TSuite done
	}

}
