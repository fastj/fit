package org.fastj.fit.func;

import java.io.File;
import java.io.FilenameFilter;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

/**
 * List File
 * 
 * @author zhouqingquan
 *
 */
public class FSListFunc implements IFunction{

	@Override
	public String name() {
		return "fs_list";
	}

	@Override
	public String frun(ParameterTable table, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length != 2) {
			throw new DataInvalidException("Func[fs_list] requires 2 parameters.");
		}
		
		String dir = StringUtil.expend(args[0], table);
		String fnamePattern = StringUtil.expend(args[1], table);
		
		File fd = new File(dir);
		if (!fd.exists()) {
			throw new DataInvalidException("Func[fs_list] dir not exists.");
		}
		
		File[] fs = fd.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				try {
					return name.endsWith(fnamePattern) || name.contains(fnamePattern) || name.matches(fnamePattern);
				} catch (Throwable e) {
					return false;
				}
			}
		});
		
		StringBuilder buff = new StringBuilder("@data:");
		for (File f : fs)
		{
			buff.append("\"").append(f.getAbsolutePath()).append("\"").append(", ");
		}
		
		if (buff.length() > 6)
		{
			buff.deleteCharAt(buff.length() - 1);
			buff.deleteCharAt(buff.length() - 1);
		}
		
		return buff.toString();
	}
	
	
	
}
