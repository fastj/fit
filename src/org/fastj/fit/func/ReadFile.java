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

import static org.fastj.fit.tool.StringUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.IFunction;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;

/**
 * @command fread(file_path, [encoding : utf-8])
 * 
 * @author zhouqingquan
 *
 */
public class ReadFile implements IFunction{

	@Override
	public String name() {
		return "fread";
	}

	@Override
	public String frun(ParameterTable table, String ... args) throws ParamIncertitudeException, DataInvalidException {
		
		if (args == null || args.length == 0)
		{
			throw new DataInvalidException("Func [read_txt] requires a file.");
		}
		
		String path = trim(expend(args[0], table));
		File f = new File(path);
		
		if (!f.exists()) return "not_exist";
		
		String encoding = args.length == 2 ? trim(expend(args[1], table)) : "utf-8";
		return read(f, encoding);
	}

	private String read(File f, String encoding)
	{
		StringBuilder rlt = new StringBuilder();
		try (FileInputStream fin = new FileInputStream(f);){
			
			byte[] buff = new byte[1024];
			int len = -1;
			while((len = fin.read(buff)) > 0)
			{
				rlt.append(new String(buff, 0, len, encoding));
			}
			return rlt.toString();
		} catch (FileNotFoundException e) {
			return "not_exist";
		} catch (IOException e1) {
			return e1.getMessage();
		}
	}
}
