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

package org.fastj.fit.tool;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.net.protocol.KeyModeParam;
import org.fastj.net.protocol.Protocol;
import org.fastj.net.protocol.SnmpPara;
import org.fastj.net.protocol.UserModeParam;

/**
 * SNMP SSH参数处理
 * 
 * @author zhouqingquan
 *
 */
public class ProtocolTool {
	
	public static SnmpPara getSnmpProtocol(ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		String pstr = table.getPara("snmp_host", null);
		if (pstr == null) throw new ParamIncertitudeException("Not find var[snmp_host]");
		
		String [] args = StringUtil.readFuncParam(pstr);
		for (int i = 0; i < args.length; i++)
		{
			args[i] = StringUtil.expend(args[i], table);
		}
		
		if (args.length == 2)
		{
			SnmpPara para = new SnmpPara();
			para.setKey("SNMPv2c");
			para.setPort(table.getInt("snmp_port", 161));
			para.setRetries(table.getInt("snmp_retry", 1));
			para.setTimeout(table.getInt("snmp_timeout", 1));
			para.setReadCommunity(args[0]);
			para.setWriteCommunity(args[1]);
			return para;
		}
		else if (args.length == 7)
		{
			SnmpPara para = new SnmpPara();
			para.setKey("SNMPv3");
			para.setPort(table.getInt("snmp_port", 161));
			para.setRetries(table.getInt("snmp_retry", 1));
			para.setTimeout(table.getInt("snmp_timeout", 1));
			para.setSecurityName(args[0]);
			para.setContextName("None".equals(args[1]) ? null : args[1]);
			para.setContextEngineId("None".equals(args[2]) ? null : args[2]);
			para.setAuthProtocol(args[3]);
			para.setAuthPassword(args[4]);
			para.setPrivProtocol(args[5]);
			para.setPrivPassword(args[6]);
			
			return para;
		}
		
		throw new DataInvalidException("Snmp Protocol Invalid: " + pstr);
	}
	
	/**
	 * @example protocolStr : host1 (host1 = 127.0.0.1,22,user,name)
	 * @example protocolStr : ${host1} (host1 = 127.0.0.1,22,user,name)
	 * @example protocolStr : 127.0.0.1, 22, user, name
	 * @example protocolStr : 127.0.0.1, 22, user, key, pass
	 * 
	 * @param protocolStr                 SSH Connect Parameters
	 * @param table                       Variables
	 * @return Protocol
	 * @throws ParamIncertitudeException
	 * @throws DataInvalidException
	 */
	public static Protocol getSSHProtocol(String protocolStr, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		protocolStr = table.getPara(protocolStr, protocolStr);
		protocolStr = StringUtil.expend(protocolStr, table);
		
		String [] args = StringUtil.readFuncParam(protocolStr);
		for (int i = 0; i < args.length; i++)
		{
			args[i] = StringUtil.expend(args[i], table);
		}
		
		if (args.length == 4)
		{
			UserModeParam ump = new UserModeParam();
			ump.setIpAddress(args[0]);
			ump.setPort(Integer.valueOf(args[1]));
			ump.setUser(args[2]);
			ump.setPassword(args[3]);
			return ump;
		}
		else if (args.length == 5)
		{
			KeyModeParam kmp = new KeyModeParam();
			
			kmp.setIpAddress(args[0]);
			kmp.setPort(Integer.valueOf(args[1]));
			kmp.setUser(args[2]);
			kmp.setPasskey(args[3]);
			kmp.setPassphrase(args[4]);
			
			return kmp;
		}
		
		throw new DataInvalidException("SSH Protocol Invalid: " + protocolStr);
	}
	
}
