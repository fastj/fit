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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.log.LogUtil;
import org.fastj.fit.model.TProject;
import org.fastj.fit.model.TestCase;

/**
 * 
 * 
 * @author zhouqingquan
 *
 */
public class HBTask extends TimerTask{

	private TestCase hb;
	private boolean block = false;
	
	public static void registHB(TestCase hbtc, String hbStr)
	{
		if (hbStr == null || hbtc == null) return;
		
		String[] hbpars = hbStr.split(",");
		int interval = 0;
		boolean block = false;
		
		try {
			interval = Integer.valueOf(hbpars[0].trim());
			block = hbpars.length == 1 ? false : Boolean.valueOf(hbpars[1].trim());
		} catch (NumberFormatException e) {
			LogUtil.error("Invalid heartbeat property '{}' , testcase {}, please check scripts.", hbStr, hbtc.getTid());
		}
		
		Timer t = new Timer(true);
		t.scheduleAtFixedRate(new HBTask(hbtc, block), interval, interval);
		LogUtil.trace("HeartBeat '{}' started.", hbtc.getTid());
	}
	
	public HBTask(TestCase hbtc, boolean block) {
		this.hb = hbtc;
		this.block = block;
	}
	
	@Override
	public void run() {
		
		if (hb == null) return;
		
		TProject tp = hb.getProject();
		if (block)
		{
			Lock lock = tp.getHblock().writeLock();
			lock.lock();
			
			try {
				TRun.run(tp, hb, null);
			} catch (ParamIncertitudeException | DataInvalidException e) {
				LogUtil.error("HeartBeat(b) fail: e={}", e.getMessage());
			}
			finally
			{
				lock.unlock();
			}
		}
		else
		{
			try {
				TRun.run(tp, hb, null);
			} catch (ParamIncertitudeException | DataInvalidException e) {
				LogUtil.error("HeartBeat fail: e={}", e.getMessage());
			}
		}
		
	}
	
}
