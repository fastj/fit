package org.fastj.fit.func;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

public class LMatch implements IFunction{

	@Override
	public String name() {
		return "lmatch";
	}

	@Override
	public String frun(ParameterTable table, String ... args) throws ParamIncertitudeException, DataInvalidException {
		int g = 0;
		String regex;
		String str;
		if (args.length == 2)
		{
			regex = StringUtil.expend(args[0], table);
			str = StringUtil.expend(args[1], table);
		}
		else if (args.length == 3)
		{
			regex = StringUtil.expend(args[0], table);
			g = Integer.valueOf(StringUtil.expend(args[1], table));
			str = StringUtil.expend(args[2], table);
		}
		else
		{
			throw new DataInvalidException("Func[pt_get]: 2 or 3 parameters. args.length=" + args.length);
		}
		
		StringBuilder buff = new StringBuilder("@data:");
		Matcher m = Pattern.compile(regex).matcher(str);
		if (m.find())
		{
			buff.append("\"").append(m.group(g)).append("\"").append(", ");
		}
		
		if (buff.length() > 6)
		{
			buff.deleteCharAt(buff.length() - 1);
			buff.deleteCharAt(buff.length() - 1);
		}
		
		return buff.toString();
	}

}
