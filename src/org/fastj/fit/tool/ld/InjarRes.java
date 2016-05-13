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
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.fastj.fit.log.LogUtil;

public class InjarRes {
	
	public JarFile baseJar;
	
	public String entry;
	
	public InputStream openStream()
	{
		ZipEntry zi = baseJar.getEntry(entry);
		if (zi == null) {
			LogUtil.error("Not find entry {} in fit jar.", entry);
			return null;
		}
		
		try {
			return baseJar.getInputStream(zi);
		} catch (IOException e) {
			LogUtil.error("Open entry {} fail.", entry);
			return null;
		}
	}
}
