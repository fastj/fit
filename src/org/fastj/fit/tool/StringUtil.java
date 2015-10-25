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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.func.Funcs;
import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.intf.ParamIncertitudeException;
import org.fastj.fit.intf.Parameter;
import org.fastj.fit.intf.ParameterTable;
import org.fastj.fit.model.Consts;

/**
 * FIT 展开机制等
 * 
 * @author zhouqingquan
 *
 */
public final class StringUtil {
	
	public static final String FUNC_CALL_REGEX = "^[A-Za-z0-9_]{1,}\\([\\S\\s]*\\)$";
	public static final String SINGLEVAR_REGEX = "^(\\$\\{[a-zA-Z0-9_\\.]{1,128}\\})$";
	
	public static boolean equals(String str1, String str2)
	{
		return str1 == null ? str2 == null : str1.equals(str2);
	}
	
	public static boolean isValidVar(String str)
	{
		return str == null ? false : !str.trim().isEmpty();
	}
	
	public static boolean isMatch(String pattern, String str)
	{
		if (pattern == null || str == null) return false;
		
		try {
			return Pattern.matches(pattern, str);
		} catch (Throwable e) {
			//TODO
			return false;
		}
	}
	
	public static final List<ParameterTable> splits(ParameterTable table, String[] lvars) throws ParamIncertitudeException, DataInvalidException
	{
		List<ParameterTable> rlt = new ArrayList<ParameterTable>();
		
		if (lvars == null || lvars.length == 0)
		{
			rlt.add(table);
			return rlt;
		}
		
		String vvs[] = new String[lvars.length];
		
		for (int i = 0;i < vvs.length; i++)
		{
			vvs[i] = expend("${" + lvars[i] + "}", table);
		}
		
		List<String[]> vvlist = new ArrayList<String[]>();
		int len = 1;
		for (String vv : vvs)
		{
			String svv[] = splits(vv);
			len = svv.length > 1 && len == 1 ? svv.length : len;
			if (svv.length != 1 && len != svv.length)
			{
				throw new DataInvalidException("Invalid loop var setting.");
			}
			
			vvlist.add(svv);
		}
		
		for (int i = 0; i < len; i++)
		{
			ParameterTable ptable = table.copy();
			int tag = 0;
			for (String v[] : vvlist)
			{
				ptable.add(lvars[tag++], v.length == 1 ? v[0] : v[i]);
			}
			rlt.add(ptable);
		}
		
		return rlt;
	}
	
	public static final String[] splits(String vstr) throws DataInvalidException
	{
		if (vstr == null || vstr.isEmpty()) return new String[0];
		
		if (vstr.startsWith(Consts.PATTERN_DATATABLE))
		{
			vstr = vstr.substring(Consts.PATTERN_DATATABLE.length());
		}
		
		String [] vs = readFuncParam(vstr);
		
		List<String> l = new ArrayList<String>();
		for (String v : vs)
		{
			if (v.matches("^.*\\(([0-9]{1,})\\)$"))
			{
				Matcher m = Pattern.compile("^(.*)\\(([0-9]{1,})\\)$").matcher(v);
				m.find();
				String rv = m.group(1);
				int n = Integer.valueOf(m.group(2));
				rv = trim(rv);
				for (int i = 0; i < n;i++)
				{
					l.add(rv);
				}
			}
			else
			{
				l.add(v);
			}
		}
		
		return l.toArray(new String[l.size()]);
	}
	
	public static String expendVar(String var,ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		Parameter p = table.get(var);
		if (p == null) throw new ParamIncertitudeException(var + " is not provided.");
		
		return expend(p.getValue(), table);
	}
	
	public static String expend(String content, ParameterTable table) throws ParamIncertitudeException, DataInvalidException
	{
		if (content == null || (!content.contains("${") && !content.matches(FUNC_CALL_REGEX)))
		{
			return content;
		}
		
		//Single variable
		if (StringUtil.isMatch(SINGLEVAR_REGEX, content))
		{
			String pname = content.substring(2, content.length() - 1);
			
			Parameter p = table.get(pname);
			if (p != null)
			{
				return expend(p.getValue(), table);
			}
			
			throw new ParamIncertitudeException("Parameter[" + pname + "] not found in table.");
		}
		
		//call function
		if (content.matches(FUNC_CALL_REGEX))
		{
			StringBuilder buff = new StringBuilder();
			char c = ' ';
			int i = 0;
			while((c = content.charAt(i++)) != '(')
			{
				buff.append(c);
			}
			String funcName = buff.toString().trim();
			List<String> params = new ArrayList<String>();
			int len = StringUtil.readCFParamStr(content, i, params);
			
			String rc = Funcs.runFunc(funcName, params, table);
			
			return expend(rc + content.substring(len, content.length()), table);
		}
		
		StringBuilder buff = new StringBuilder(content.length() + 256);

		int offset = 0;
		int idx = -1;
		while((idx = content.indexOf("${", offset)) >= 0)
		{
			StringBuilder body = new StringBuilder();
			buff.append(content.substring(offset, idx));
			int sidx = StringUtil.readExpendBody(content, idx + 2, body);
			if (sidx > 0)
			{
				offset = sidx;
				String bstr = body.toString();
				String expv = expend(bstr, table);
				if (bstr.matches(FUNC_CALL_REGEX))
				{
					buff.append(expv);
				}
				else
				{
					Parameter p = table.get(expv);
					if (p == null)
					{
						throw new ParamIncertitudeException("Parameter[" + expv + "] not found in table.");
					}
					
					buff.append(expend(p.getValue(), table));
				}
			}
			else
			{
				break;
			}
		}
		buff.append(content.substring(offset, content.length()));
		return buff.toString().trim();
	}
	
	public static int readExpendBody(String line, int start, StringBuilder body)
	{
		//${[A-Za-z_0-9](.*)}
		int len = line.length();
		int bigbCnt = 0;
		int midbCnt = 0;
		int litbCnt = 0;
		int status = 0; //1 in ""
		int i = start;
		
		for (; i < len; i++)
		{
			char c = line.charAt(i);
			if (c == '"')
			{
				status = (++status) % 2;
			}
			
			if (status == 0)
			switch(c)
			{
				case '{': bigbCnt++; break;
				case '}': bigbCnt--; break;
				case '[': midbCnt++; break;
				case ']': midbCnt--; break;
				case '(': litbCnt++; break;
				case ')': litbCnt--; break;
			}
			
			//End
			if (c == '}' && bigbCnt == -1 && midbCnt == 0 && litbCnt == 0)
			{
				return i + 1; //length
			}
			
			body.append(c);
		}
		
		return -1;
	}
	
	/**
	 * @param line
	 * @param rlt
	 * @return int : read length
	 */
	public static int readCFParamStr(String line, int start, List<String> rlt)
	{
		//$[A-Za-z_0-9](.*)
		int len = line.length();
		int bigbCnt = 0;
		int midbCnt = 0;
		int litbCnt = 0;
		int status = 0; //1 in ""
		StringBuilder buff = new StringBuilder();
		
		int i = start;
		for (; i < len; i++)
		{
			char c = line.charAt(i);
			if (c == '"')
			{
				status = (++status) % 2;
			}
			
			if (status == 0)
			switch(c)
			{
				case '{': bigbCnt++; break;
				case '}': bigbCnt--; break;
				case '[': midbCnt++; break;
				case ']': midbCnt--; break;
				case '(': litbCnt++; break;
				case ')': litbCnt--; break;
			}
			
			//End
			if (c == ')' && litbCnt == -1 && bigbCnt == 0 && midbCnt == 0)
			{
				if (buff.length() > 0 || !rlt.isEmpty())
				{
					String iv = trim(buff.toString());
					if (!iv.isEmpty() || !rlt.isEmpty()) rlt.add(iv);
				}
				return i + 1; //length
			}
			
			if (status == 0 && bigbCnt == 0 && midbCnt == 0 && litbCnt == 0 && c == ',')
			{
				rlt.add(trim(buff.toString()));
				buff.delete(0, buff.length());
				continue;
			}
			
			buff.append(c);
		}
		
		return -1;
	}
	
	
	public static String[] readFuncParam(String fparam) throws DataInvalidException
	{
		if (fparam == null || fparam.isEmpty()) return new String[]{};
		List<String> l = new ArrayList<String>();
		int len = readCFParamStr(fparam + ")", 0, l);
		if (len != fparam.length() + 1)
		{
			throw new DataInvalidException("Func param invalid: " + fparam);
		}
		return l.toArray(new String[l.size()]);
	}
	
	public static String[] readCmdParam(String fparam)
	{
		return readCmdParam(fparam, true);
	}
	
	public static String[] readCmdParam(String fparam, boolean trim)
	{
		if (fparam == null || fparam.trim().isEmpty())
		{
			return new String[0];
		}
		
		fparam = fparam.trim();
		
		List<String> fparas = new ArrayList<String>();
		
		StringBuilder buff = new StringBuilder();
		int status = 0; // 1 in ""
		for (int i = 0; i < fparam.length(); i++)
		{
			char c = fparam.charAt(i);
			if (c == '"')
			{
				status = (++status) % 2;
				buff.append(c);
				continue;
			}
			else if (c == ' ')
			{
				if (status == 0)
				{
					for (;fparam.charAt(i+1) == ' ';i++) ;
					
					fparas.add(trim ? trim(buff.toString()) : buff.toString().trim());
					buff.delete(0, buff.length());
					continue;
				}
				
				buff.append(c);
			}
			else
			{
				buff.append(c);
			}
		}
		
		//last
		fparas.add(trim(buff.toString()));
		
		return fparas.toArray(new String[fparas.size()]);
	}
	
	public static String sendStr(String str)
	{
		int len = str.length();
		if (str.charAt(len - 2) == '\\' && str.charAt(len - 1) == 'n')
		{
			return str.substring(0, len - 2) + "\n";
		}
		
		return str;
	}
	
	public static String trim(String str)
	{
		if (str == null) return null;
		String rlt = str.trim();
		if (rlt.isEmpty()) return rlt;
		if (rlt.charAt(0) == '"' && rlt.charAt(rlt.length() - 1) == '"')
		{
			rlt = rlt.substring(1, rlt.length() - 1);
		}
		return rlt;
	}
}
