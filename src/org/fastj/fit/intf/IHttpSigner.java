package org.fastj.fit.intf;

import java.util.Map;

public interface IHttpSigner {
	
	String name();
	
	void sign(String method, String url, String content, Map<String, String> headers, ParameterTable table, String ... args);
	
}
