package org.fastj.fit.intf;

import java.util.HashMap;
import java.util.Map;

public class HttpSignFactory {

	private static HashMap<String, IHttpSigner> signers = new HashMap<>();

	public static void regist(IHttpSigner signer) {
		signers.put(signer.name(), signer);
	}

	public static void sign(String signName, String method, String url, String content, Map<String, String> headers,
			ParameterTable table, String... args) throws ParamIncertitudeException, DataInvalidException {
		IHttpSigner signer = signers.get(signName);
		if (signer != null) {
			signer.sign(method, url, content, headers, table, args);
		}
	}
}
