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

package org.fastj.fit.fcall;

import java.util.HashMap;
import java.util.Map;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.net.api.CmdLine;
import org.fastj.net.api.Response;
import org.fastj.net.impl.CmdLineImpl;
import static org.fastj.fit.tool.StringUtil.*;

/**
 * @command cmd(command)
 * 
 * @param cmd_env = env1, envValue1, env2, envValue2
 * @param cmd_autosend = keyStr1, value1, keyStr2, value2
 * @param cmd_timeout = 15000
 * 
 * @author zhouqingquan
 *
 */
public class CmdFunc implements IFuncCall{

	@Override
	public String name() {
		return "cmd";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		
		CmdLine cmd = new CmdLineImpl();
		
		String[] envs = readFuncParam(expend(table.getPara("cmd_env", ""), table));
		String[] autos = readFuncParam(expend(table.getPara("cmd_autosend", ""), table));
		if (envs.length % 2 == 0 && autos.length % 2 == 0)
		{
			for (int i = 0; i < envs.length - 2; i += 2)
			{
				cmd.env(envs[i], envs[i + 1]);
			}
			
			for (int i = 0; i < autos.length - 2; i += 2)
			{
				cmd.autosend(autos[i], sendStr(autos[i + 1]));
			}
		}
		else
		{
			throw new DataInvalidException("Env or AutoSend invalid.");
		}
		
		Response<String> resp = cmd.exec(table.getInt("cmd_timeout", 30000), readCmdParam(expend(argStr, table)));
		
		FuncResponse fr = new FuncResponse();
		fr.setCode(resp.getCode());
		fr.setPhrase(resp.getPhrase());
		fr.setRequest(argStr);
		
		Map<String, Object> entity = new HashMap<String, Object>();
		entity.put("content", resp.getEntity());
		entity.put("phrase", resp.getPhrase());
		entity.put("code", resp.getCode());
		fr.setEntity(entity);
		
		return fr;
	}

}
