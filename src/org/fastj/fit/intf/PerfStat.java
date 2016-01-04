package org.fastj.fit.intf;

public interface PerfStat {
	
	void put(String key, long start, int time);
	
	String report(String type);
	
	class PerfNode
	{
		public long start;
		public int time;
	}
}
