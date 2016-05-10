/*
 * Copyright 2015  FastJ
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastj.fit.func;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;

public class UrLEncode implements IFunction{

	@Override
	public String name() {
		return "url_encode";
	}

	@Override
	public String frun(ParameterTable ptable, String... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 1) return "";
		
		try {
			return URLEncoder.encode(args[0], args.length > 1 ? args[1] : "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new ParamIncertitudeException("uri_encode error: " + e.getMessage());
		}
	}

}
