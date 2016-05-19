package org.fastj.fit.func;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

/**
 * @command for(var : list, expr)
 * 
 * list: @data:v1,v2,v3   /   [v1,v2,v3]
 * list: [1-9], [1-9:2]
 * 
 * @author zhouqingquan
 *
 */
public class ForFunc implements IFunction{

	@Override
	public String name() {
		return "for";
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length != 2) {
			throw new DataInvalidException("Func[for] requires 2 parameters.");
		}
		
		String pars[] = args[0].split(":", 2);
		String varName = pars[0].trim();
		if (pars.length != 2 || varName.isEmpty() || pars[1].trim().isEmpty()) {
			throw new DataInvalidException("Func[for] invalid parameter[0]: " + args[0]);
		}
		
		List<String> vvs = new ArrayList<>();
		parseList(vvs, pars[0], ptable);
		String expr = args[1];
		
		for (String var : vvs) {
			ptable.add(varName, var);
			StringUtil.expend(expr, ptable);
		}
		
		return "OK";
	}
	
	private void parseList(List<String> l, String list, ParameterTable table) throws ParamIncertitudeException, DataInvalidException {
		
		String lstr = StringUtil.expend(list, table).trim();
		if (lstr.startsWith("@data:")) {
			lstr = lstr.substring(6);
			String [] vs = StringUtil.readCmdParam(lstr);
			for (String v : vs) l.add(v);
			return;
		}
		
		if (lstr.charAt(0) != '[' || lstr.charAt(lstr.length() - 1) != ']') {
			throw new DataInvalidException("Func[for] invalid list expr.");
		}
		
		lstr = lstr.substring(1, lstr.length() - 1).trim();
		if (lstr.contains(",")) {
			String [] vs = StringUtil.readCmdParam(lstr);
			for (String v : vs) l.add(v);
			return;
		}
		
		String regex = "^([0-9]{1,})-([0-9]{1,})(:([0-9]{1,}))*$";
		Matcher m = Pattern.compile(regex).matcher(lstr);
		if (m.find()) {
			int s = Integer.valueOf(m.group(1));
			int e = Integer.valueOf(m.group(2));
			int step = m.groupCount() > 5 ? Integer.valueOf(m.group(4)) : 1;
			for (int i = s; i <= e; i += step) {
				l.add(String.valueOf(i));
			}
			return;
		}
		
		throw new DataInvalidException("Func[for] invalid list expr.");
	}

}
