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

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.tool.ProtocolTool;
import org.fastj.fit.tool.StringUtil;
import org.fastj.net.api.NVar;
import org.fastj.net.api.Response;
import org.fastj.net.api.SshConnection;
import org.fastj.net.impl.JSchImpl;
import org.fastj.net.protocol.Protocol;

/**
 * 
 * @command ssh_connect(ip, port, user, pass)
 * @command ssh_connect(ip, port, user, keystore, pass)
 * @command ssh_exec(cmd)
 * @command ssh_close()
 * 
 * @param autosend="password: ", "pass\\n"
 * @param ends=>, ]
 * @param timeout=1000
 * @param clean_setting=true
 * 
 * @author zhouqingquan
 *
 */
public class SSHFunc implements IFuncCall{

	private String name;
	
	public SSHFunc(String func)
	{
		this.name = func;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		
		switch(name)
		{
		case "ssh_connect":
			Protocol ptc = ProtocolTool.getSSHProtocol(argStr, table);
			if (ptc == null) throw new DataInvalidException("Cannot create SSH Protocol: " + argStr);
			SshConnection ssh = new JSchImpl();
			setConn(ssh, table);
			Response<String> sshresp = ssh.open(ptc);
			FuncResponse fr = new FuncResponse();
			fr.setRequest("ssh_connect " + ptc.toString());
			if (ssh.isConnected())
			{
				ctx.put("__ssh_connection__", ssh);
				
				fr.setCode(Response.OK);
				HashMap<String, Object> entity = new HashMap<String, Object>();
				entity.put("code", 0);
				entity.put("message", "SSH Connected.");
				fr.setEntity(entity);
				return fr;
			}
			else
			{
				fr.setCode(sshresp.getCode());
				fr.setPhrase(sshresp.getPhrase());
				HashMap<String, Object> entity = new HashMap<String, Object>();
				entity.put("code", sshresp.getCode());
				entity.put("message", sshresp.getPhrase());
				fr.setEntity(entity);
				
				return fr;
			}
			
		case "ssh_exec":
			SshConnection ssh_conn = ctx.get("__ssh_connection__");
			String cmd = StringUtil.expend(argStr, table);
			
			if (ssh_conn != null)
			{
				setConn(ssh_conn, table);
				int timeout = 15000;
				try {
					timeout = Integer.valueOf(table.getPara("timeout", "15000"));
				} catch (NumberFormatException e) {}
				Response<String> resp = ssh_conn.exec(timeout, cmd);
				FuncResponse exefr = new FuncResponse();
				exefr.setCode(resp.getCode());
				exefr.setRequest("ssh_exec " + cmd);
				HashMap<String, Object> entity = new HashMap<String, Object>();
				entity.put("content", resp.getEntity());
				entity.put("message", resp.getPhrase());
				entity.put("code", resp.getCode());
				
				exefr.setEntity(entity);
				exefr.setPhrase(resp.getPhrase());
				exefr.setRequest(cmd);
				return exefr;
			}
			else
			{
				FuncResponse exefr = new FuncResponse();
				exefr.setCode(Response.INTERNAL_ERROR);
				exefr.setRequest("ssh_exec " + cmd);
				HashMap<String, Object> entity = new HashMap<String, Object>();
				entity.put("message", "No SSH connection.");
				entity.put("code", Response.INTERNAL_ERROR);
				exefr.setEntity(entity);
				exefr.setPhrase("No SSH connection.");
				return exefr;
			}
			
		case "ssh_close":
			SshConnection sshconn = ctx.get("__ssh_connection__");
			if (sshconn != null)
			{
				sshconn.close();
			}
			FuncResponse clfr = new FuncResponse();
			clfr.setCode(Response.OK);
			clfr.setRequest("ssh_close()");
			clfr.setEntity(new HashMap<String, Object>());
			return clfr;
		}
		
		throw new DataInvalidException("Unsupport SSH Func: " + name);
	}
	
	/**
	 * autosend("password: ", 123\n, "yes/no: ", yes\n)
	 * ends(>, #)
	 * timtout(5000)
	 * clean_setting()
	 * 
	 * @param ssh
	 * @param table
	 */
	private void setConn(SshConnection ssh, ParameterTable table) throws DataInvalidException
	{
		if (table.lcontains("clean_setting"))
		{
			ssh.clean();
		}
		
		if (table.lcontains("autosend"))
		{
			String astr = table.get("autosend").getValue();
			String[] ass = StringUtil.readFuncParam(astr);
			if (ass.length % 2 == 0)
			{
				NVar nvar = new NVar();
				for (int i = 0; i <= ass.length - 2; i += 2)
				{
					nvar.add(ass[i], ass[i + 1]);
				}
				ssh.with(nvar);
			}
			else
			{
				throw new DataInvalidException("autosend invalid: " + astr);
			}
		}
		
		if (table.lcontains("ends"))
		{
			String endstr = table.get("ends").getValue();
			String[] ess = StringUtil.readFuncParam(endstr);
			ssh.with(ess);
		}
		
	}
	
}
