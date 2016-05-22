package org.fastj.fit.func;

import java.util.Arrays;
import java.util.Comparator;

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
		
		String[] dl = parse(data);
		
		Arrays.sort(dl, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.valueOf(o1) > Double.valueOf(o2) ? 1 : -1;
			}
		});
		if (ASC.equals(op.toLowerCase())){
			return format(dl);
		}
		
		for (int i = 0; i < dl.length/2 ; i++){
			String t = dl[i];
			dl[i] = dl[dl.length - i - 1];
			dl[dl.length - i - 1] = t;
		}
		return format(dl);
	}
	
	private String format(String [] dl) {
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
	
	private String[] parse(String data) throws DataInvalidException{
		if (data.startsWith("@data:")) {
			data = data.substring(6);
		}
		String [] pars = StringUtil.readFuncParam(data);
		return pars;
	}

}
