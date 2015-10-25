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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.tool.ProtocolTool;
import org.fastj.fit.tool.StringUtil;
import org.fastj.net.api.Response;
import org.fastj.net.api.SftpConnection;
import org.fastj.net.impl.SftpJSchImpl;
import org.fastj.net.protocol.Protocol;

/**
 * 
 * @command sftp_upload(rdir, rFileName, localFile)
 * @command sftp_download(rdir, rFileName, localFile)
 * 
 * @param ssh_host=ip, port, user, pass / ip, port, user, keystore, pass
 * 
 * @author zhouqingquan
 *
 */
public class SftpFunc implements IFuncCall{

	private String name;
	
	public SftpFunc(String func)
	{
		this.name = func;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		FuncResponse fr = new FuncResponse();
		
		Protocol ptc = ProtocolTool.getSSHProtocol(StringUtil.expendVar("ssh_host", table), table);
		String[] args = StringUtil.readFuncParam(argStr);
		
		Map<String, Object> entity = new HashMap<String, Object>();
		
		if (args.length != 3)
		{
			fr.setCode(Response.INVALID_PARAM);
			fr.setPhrase("Param invalid(len != 3): " + argStr);
			fr.setRequest(name + " " + argStr);
			entity.put("code", Response.INVALID_PARAM);
			entity.put("message", "Param invalid(len != 3): " + argStr);
			fr.setEntity(entity);
			
			return fr;
		}
		
		for (int i = 0; i < args.length; i++)
		{
			args[i] = StringUtil.expend(args[i], table);
		}
		
		SftpConnection sftp = new SftpJSchImpl();
		try {
			Response<String> resp = sftp.open(ptc);
			if (resp.getCode() != Response.OK)
			{
				fr.setCode(resp.getCode());
				fr.setPhrase(resp.getPhrase());
				fr.setRequest(name + " " + argStr);
				
				entity.put("code", resp.getCode());
				entity.put("message", resp.getPhrase());
				fr.setEntity(entity);
				
				return fr;
			}
			
			if ("sftp_upload".equals(name))
			{
				resp = sftp.upload(args[0], args[1], args[2]);
			}
			else
			{
				resp = sftp.download(args[0], args[1], args[2]);
			}
			
			sftp.close();
			
			fr.setCode(resp.getCode());
			fr.setPhrase(resp.getPhrase());
			fr.setRequest(name + " " + Arrays.asList(args));
			
			entity.put("code", resp.getCode());
			entity.put("message", resp.getPhrase());
			fr.setEntity(entity);
			
			return fr;
		} 
		finally
		{
			sftp.close();
		}
		
	}
	
}
