package org.fastj.fit.func;

import java.util.Arrays;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

/**
 * @command sort(${dataVar}[, op : asc])
 * 
 * 
 * @author zhouqingquan
 *
 */
public class SortDataFunc implements IFunction{

	public static final String ASC = "asc";
	public static final String DESC = "desc";
	
	@Override
	public String name() {
		return "sort";
	}

	@Override
	public String frun(ParameterTable table, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 1 || args.length > 2) {
			throw new DataInvalidException("Func[sort] requires 1 or 2 parameters.");
		}
		
		String data = StringUtil.expend(args[0], table);
		String op = args.length == 2 ? StringUtil.expend(args[1], table) : ASC;
		
		double[] dl = parse(data);
		
		Arrays.sort(dl);
		if (ASC.equals(op.toLowerCase())){
			return format(dl);
		}
		
		for (int i = 0; i < dl.length/2 ; i++){
			double t = dl[i];
			dl[i] = dl[dl.length - i];
			dl[dl.length - i] = t;
		}
		return format(dl);
	}
	
	private String format(double [] dl) {
		StringBuilder buff = new StringBuilder();
		buff.append("@data:");
		if (dl.length > 0) {
			for (int i = 0; i < dl.length - 1; i++)
			{
				buff.append(dl[i]).append(",");
			}
			buff.append(dl[dl.length - 1]);
		}
		
		return buff.toString();
	}
	
	private double[] parse(String data) throws DataInvalidException{
		if (data.startsWith("@data:")) {
			data = data.substring(6);
		}
		
		String [] pars = StringUtil.readCmdParam(data);
		double rlt [] = new double[pars.length];
		for (int i = 0; i < rlt.length; i++) {
			try {
				rlt[i] = Double.valueOf(pars[i].trim());
			} catch (NumberFormatException e) {
				throw new DataInvalidException(rlt[i] + " is not a number.");
			}
		}
		
		return rlt;
	}

}
