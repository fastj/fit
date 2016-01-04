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

package org.fastj.fit.tool.ld;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.fastj.fit.tool.TSysInit;

public class InjarUrlStreamHander extends URLStreamHandler {
	public InjarUrlStreamHander() {
	}

	protected URLConnection openConnection(URL u) throws IOException {
		InputStream ins = TSysInit.getResourceStream(u.getFile());
		if (ins == null) return null;
		return new InnerUrlConnection(u, ins);
	}

	protected void parseURL(URL url, String spec, int start, int limit) {
		String file = spec;
		if (spec.startsWith("injar:"))
			file = spec.substring(6);
		super.setURL(url, "injar", "", -1, null, null, file, null, null);
	}
}
