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

import static org.fastj.fit.tool.StringUtil.expend;
import static org.fastj.fit.tool.StringUtil.trim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;

/**
 * @command fwrite(file, content [, encoding: utf-8])
 * 
 * @author zhouqingquan
 *
 */
public class WriteFile implements IFunction{

	@Override
	public String name() {
		return "fwrite";
	}

	@Override
	public String frun(ParameterTable table, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length < 2)
		{
			throw new DataInvalidException("Func[fwrite] requires 2 parameters.");
		}
		
		String path = trim(expend(args[0], table));
		String value = expend(args[1], table);
		File f = new File(path);
		
		if (!f.exists()) return "file_not_exist";
		
		String encoding = args.length == 3 ? trim(expend(args[2], table)) : "utf-8";
		
		try (FileOutputStream fout = new FileOutputStream(f);) {
			fout.write(value.getBytes(encoding));
			fout.flush();
		} catch (IOException e) {
			return "write_fail:" + e.getMessage();
		}
		
		return "OK";
	}

}
