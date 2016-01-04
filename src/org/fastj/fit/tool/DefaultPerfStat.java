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

import java.util.ArrayList;
import java.util.List;

import org.fastj.fit.intf.PerfStat;

public class DefaultPerfStat implements PerfStat{

	private List<PerfNode> stats = new ArrayList<>();
	
	@Override
	public void put(String key, long start, int time) {
		PerfNode pn = new PerfNode();
		pn.start = start;
		pn.time = time;
		stats.add(pn);
	}

	@Override
	public String report(String type) {
		StringBuilder buff = new StringBuilder();
		for (PerfNode e : stats)
		{
			buff.append(e.start).append(" : ").append(e.time).append("\r\n");
		}
		return buff.toString();
	}

}
