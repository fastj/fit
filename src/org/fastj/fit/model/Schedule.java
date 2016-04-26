package org.fastj.fit.model;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import static org.fastj.fit.tool.StringUtil.*;

public final class Schedule {
	
	private int total = 0;
	
	private int cntPerloop = 0;
	
	private int interval = 0; //ms
	
	private Schedule(){}
	
	public static Schedule parse(String expr, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		Schedule s = new Schedule();
		
		String parts[] = readFuncParam(expend(expr, table));
		
		if (parts.length == 3)
		try {
			s.total = Integer.valueOf(parts[0]);
			s.cntPerloop = Integer.valueOf(parts[1]);
			s.interval = Integer.valueOf(parts[2]);
			return s;
		} catch (NumberFormatException e) {
			throw new DataInvalidException(e.getMessage());
		}
		
		throw new DataInvalidException("Args length < 3");
	}

	public int getTotal() {
		return total;
	}
	
	public void setTotal(int num)
	{
		if (total > num) return;
		total = num;
	}

	public int getCntPerloop() {
		return cntPerloop;
	}

	public int getInterval() {
		return interval;
	}

	public Schedule copy(){
		Schedule s = new Schedule();
		s.total = total;
		s.cntPerloop = cntPerloop;
		s.interval = interval;
		return s;
	}
}
