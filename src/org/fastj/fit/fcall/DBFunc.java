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

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.FuncResponse;
import org.fastj.fit.intf.IFuncCall;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.intf.TContext;
import org.fastj.fit.tool.DBUtil;
import org.fastj.net.api.Response;
import static org.fastj.fit.tool.StringUtil.*;

import java.util.Map;

/**
 * @command $sql(dburl, user, password, sql)
 * @command $sql(${dbcfg}, sql)
 * @param dbcfg = dburl, user, password
 * 
 * @author zhouqingquan
 *
 */
public class DBFunc implements IFuncCall{

	@Override
	public String name() {
		return "sql";
	}

	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		String[] args = readFuncParam(argStr);
		
		if (args.length != 2 && args.length != 4)
		{
			throw new DataInvalidException("Func[sql] needs 2 or 4 args.");
		}
		
		String dbq[] = new String[4];
		if (args.length == 4)
		{
			for (int i = 0; i < 4; i++)
			{
				dbq[i] = expend(args[i], table);
			}
		}
		else
		{
			dbq[3] = expend(args[1], table);
			String [] dbargs = readFuncParam(expend(args[0], table));
			if (dbargs.length != 3) throw new DataInvalidException("Func[sql] DBconn needs 3 args: " + dbargs.length);
			for (int i = 0; i < 3; i++)
			{
				dbq[i] = dbargs[i];
			}
		}
		
		Map<String, Object> map = DBUtil.exec(dbq[0], dbq[1], dbq[2], dbq[3]);
		FuncResponse fr = new FuncResponse();
		fr.setRequest(dbq[3]);
		fr.setCode(Response.OK);
		fr.setEntity(map);
		
		return fr;
	}

}
