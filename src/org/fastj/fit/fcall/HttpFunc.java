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
import org.fastj.fit.intf.TProject;
import org.fastj.fit.model.Consts;
import org.fastj.fit.tool.JSONHelper;
import static org.fastj.fit.tool.StringUtil.*;

import java.io.File;

import org.fastj.net.api.HttpConnection;
import org.fastj.net.api.HttpReq;
import org.fastj.net.api.HttpRsp;
import org.fastj.net.api.Response;
import org.fastj.net.impl.HttpImpl;

/**
 * @command http_[get|put|post|delete|patch|option|head](uri|url, header1, headers2)
 * @args uri : /api/book
 * @args url : http://127.0.0.1:8080/api/book
 * @args http.endpoint : http://127.0.0.1:8080
 * 
 * 
 * @author zhouqingquan
 *
 */
public class HttpFunc implements IFuncCall{

	private String name;
	
	public HttpFunc(String aw)
	{
		name = aw;
	}
	
	@Override
	public String name() {
		return name;
	}

	/**
	 * http_method(uri, headers1, headers2)
	 * content=xx
	 * headers1=@data:Content-Type:application/json, Token:token11
	 * 
	 */
	@Override
	public FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException {
		String method = name.split("_")[1];
		String args[] = readFuncParam(argStr);
		
		for (int i = 0; i < args.length; i++)
		{
			args[i] = expend(args[i], table);
		}
		
		HttpReq hreq = new HttpReq();
		hreq.setUrl(getUrl(args[0], table));
		hreq.setMethod(method);
		hreq.setContent(expend(table.getPara("content", null), table));
		
		if (table.lcontains("download"))
		{
			String dfile = expendVar("download", table);
			hreq.setDownloadFile(dfile);
		}
		else if (table.lcontains("upload"))
		{
			TProject tp = ctx.get("__project__");
			String upstr = expendVar("upload", table);
			String par[] = upstr.split(",", 2);
			if (par.length == 2)
			{
				File f;
				String ufile = trim(expend(par[1], table));
				ufile = new File(ufile).exists() ? ufile : (f = tp.getResourceFile(ufile)).exists() ? f.getAbsolutePath() : null;
				if (ufile == null) throw new ParamIncertitudeException("Upload file not exists");
				hreq.addUpload(trim(expend(par[0], table)), ufile);
			}
			else
			{
				File f;
				String ufile = trim(expend(par[0], table));
				ufile = new File(ufile).exists() ? ufile : (f = tp.getResourceFile(ufile)).exists() ? f.getAbsolutePath() : null;
				if (ufile == null) throw new ParamIncertitudeException("Upload file not exists");
				hreq.addUpload("item", ufile);
			}
		}
		
		for ( int i = 1; i < args.length; i++)
		{
			expendHeader(args[i], table, hreq);
		}
		
		HttpConnection conn = new HttpImpl();
		Response<HttpRsp<String>> hresp = conn.exec(hreq);
		
		FuncResponse fr = new FuncResponse();
		fr.setCode(hresp.getCode());
		fr.setPhrase(hresp.getPhrase());
		fr.setRequest(hreq.toString());
		fr.setEntity(JSONHelper.getJson(hresp));
		return fr;
	}

	private String getUrl(String url, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		String turl = expend(url, table);
		
		if (turl.startsWith("http"))
		{
			return turl;
		}
		
		String http = expendVar("http.endpoint", table);
		
		return http + turl;
	}
	
	/**
	 * headExpr: @data:Content-Type: application/json, X-token: ${token}
	 *           Content-Type
	 *           
	 * @param headExpr
	 * @param ptable
	 * @param req
	 * @throws ParamIncertitudeException 
	 * @throws DataInvalidException 
	 */
	private void expendHeader(String headExpr, ParameterTable ptable, HttpReq req) throws ParamIncertitudeException, DataInvalidException
	{
		if (headExpr.startsWith(Consts.PATTERN_DATATABLE))
		{
			String expr = headExpr.substring(Consts.PATTERN_DATATABLE.length());
			String hexprs[] = readFuncParam(expr);
			for (String hexpr : hexprs)
			{
				String par[] = hexpr.split(":", 2);
				if (par.length == 2)
				{
					req.addHeader(trim(par[0]), trim(expend(par[1], ptable)));
				}
				else
				{
					req.addHeader(trim(par[0]), trim(expendVar(par[0], ptable)));
				}
			}
		}
		else
		{
			req.addHeader(headExpr, trim(expendVar(headExpr, ptable)));
		}
	}
}
