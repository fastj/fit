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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.fastj.net.api.SnmpConnection;
import org.fastj.net.api.Table;
import org.fastj.net.impl.Snmp4jImpl;

/**
 * @command snmp_get(oid1 [, oid2 ...])
 * @command snmp_set(oid, value, type)
 * @command snmp_table(col_oid1 [, col_oid2 ...])
 * 
 * @param snmp_table_timeout = 30000
 * @param snmp_host=secName,context,engineId,authProtocol,authPassword,privProtocol,privPassword
 * @param snmp_port=161
 * @param snmp_retry=1
 * @param snmp_timeout=1
 * 
 * @author zhouqingquan
 *
 */
public class SnmpFunc implements IFuncCall{

	private String name;
	
	public SnmpFunc(String snmpCmd)
	{
		name = snmpCmd;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		
		SnmpConnection snmp = new Snmp4jImpl();
		snmp.open(ProtocolTool.getSnmpProtocol(table));
		
		if (!snmp.isConnected())
		{
			FuncResponse fr = new FuncResponse();
			fr.setCode(Response.NOT_CONNECTED);
			fr.setEntity(new HashMap<String, Object>());
			fr.setPhrase(snmp.getError().getMessage());
			return fr;
		}
		
		if ("snmp_get".equals(name))
		{
			String[] oids = StringUtil.readFuncParam(StringUtil.expend(argStr, table));
			Response<NVar> resp = snmp.get(oids);
			FuncResponse fr = new FuncResponse();
			fr.setCode(resp.getCode());
			fr.setRequest("snmp_get " + argStr);
			fr.setPhrase(resp.getPhrase());
			fr.setEntity(nvar2Map(resp.getEntity()));
			return fr;
		}
		else if ("snmp_set".equals(name))
		{
			String[] args = StringUtil.readFuncParam(StringUtil.expend(argStr, table));
			Response<NVar> resp = snmp.set(args[0], args[1], Integer.valueOf(args[2]));
			FuncResponse fr = new FuncResponse();
			fr.setCode(resp.getCode());
			fr.setRequest("snmp_set " + argStr);
			fr.setPhrase(resp.getPhrase());
			fr.setEntity(nvar2Map(resp.getEntity()));
			return fr;
		}
		else if ("snmp_table".equals(name))
		{
			String[] args = StringUtil.readFuncParam(StringUtil.expend(argStr, table));
			Response<Table<NVar>> resp = snmp.table(table.getInt("snmp_table_timeout", 30000), args);
			FuncResponse fr = new FuncResponse();
			fr.setCode(resp.getCode());
			fr.setRequest("snmp_table " + argStr);
			fr.setPhrase(resp.getPhrase());
			fr.setEntity(table2Map(resp.getEntity()));
			return fr;
		}
		
		throw new DataInvalidException("Cannot reach here");
	}

	private Map<String, Object> nvar2Map(NVar var)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (String key : var.getKeys())
		{
			map.put(key, var.get(key));
		}
		
		return map;
	}
	
	private Map<String, Object> table2Map(Table<NVar> tvar)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("total", tvar.size());
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		for (NVar var : tvar.getData())
		{
			list.add(nvar2Map(var));
		}
		map.put("table", list);
		
		return map;
	}
	
}
