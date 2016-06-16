package org.fastj.fit.func;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.tool.StringUtil;

public class Base64Func implements IFunction{

	public static final String ENCODE = "b64enc";
	public static final String DECODE = "b64dec";
	
	private String name = ENCODE;
	
	public Base64Func(String key) {
		this.name = key;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public String frun(ParameterTable ptable, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 1) {
			throw new DataInvalidException("Func[" + name + "] requires 1 or 2 parameters.");
		}
		
		String str = StringUtil.expend(args[0], ptable);
		String encode = args.length > 1 ? StringUtil.expend(args[1], ptable) : "utf-8";
		
		if (ENCODE.equals(name)) {
			Encoder coder = Base64.getEncoder();
			try {
				return coder.encodeToString(str.getBytes(encode));
			} catch (UnsupportedEncodingException e) {
				throw new DataInvalidException(e.getMessage());
			}
		}
		else if (DECODE.equals(name)) {
			Decoder coder = Base64.getDecoder();
			byte[] buff = coder.decode(str);
			return new String(buff);
		}
		
		throw new DataInvalidException("Func[" + name + "] invalid.");
	}

}
