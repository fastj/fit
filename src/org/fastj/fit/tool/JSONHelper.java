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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.intf.DataInvalidException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON取值，xml转换
 * 
 * @author zhouqingquan
 *
 */
public class JSONHelper {
	
	public static Map<String, Object> getJson(String content)
	{
		if (content == null || (content = content.trim()).isEmpty())
		{
			return new HashMap<String, Object>();
		}
		Map<String, Object> jo = null;
		if (content.matches("^(\\{|\\[)[\\S\\s]*(\\}|\\])$"))
		{
			ObjectMapper mapper = new ObjectMapper();
			try {
				if (content.startsWith("{"))
				{
					jo = mapper.readValue(content, new JsonType<Map<String, Object>>());
				}
				else
				{
					Object l = mapper.readValue(content, new JsonType<List<Object>>());
					jo = new HashMap<>();
					jo.put("list", l);
				}
			} catch (Throwable e) {
			}
		}
		
		//xml
		if (jo == null)
		{
			if (content.matches("^<[\\S\\s]*>$"))
			{
				Document doc = JdomHelper.build(content);
				if (doc != null)
				{
					jo = xml2Json(doc);
				}
			}
			
			if (jo == null)
			{
				jo = new HashMap<String, Object>();
				jo.put("content", content);
			}
		}
		
		return jo;
	}
	
//	public static Map<String, Object> getJson(Response<HttpRsp<String>> hresp)
//	{
//		Map<String, Object> jo = getJson(hresp.getEntity().getContent());
//		
//		jo.put("httpcode", hresp.getEntity().getHttpCode());
//		Map<String, Object> headers = new HashMap<String, Object>();
//		jo.put("header", headers);
//		for (String h : hresp.getEntity().getHeaders().keySet())
//		{
//			headers.put(h, hresp.getEntity().getHeaders().get(h));
//		}
//		
//		return jo;
//	}
	
	public static String jsonString(Object o)
	{
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
		} catch (Throwable e) {
		}
		return null;
	}
	
	public static Map<String, Object> xml2Json(Document doc)
	{
		delNS(doc.getRootElement());
		Map<String, Object> jo = new HashMap<String, Object>();
		jo.put(doc.getRootElement().getName(), xml2Json(doc.getRootElement()));
		return jo;
	}
	
	
	public static Object jsonValue(String path, Map<String, Object> jo) throws DataInvalidException
	{
		//find(), data(), loop(), unique(), asc(), desc()
		String regex = "^[\\S\\s]*\\[((find|data|loop|unique|asc|desc)\\(\\))\\][\\S\\s]*$";
		String regex1 = "\\[((find|data|loop|unique|asc|desc)\\(\\))\\]";
		
		if (path.matches(regex))
		{
			Matcher m = Pattern.compile(regex1).matcher(path);
			String op = null;
			if (m.find())
			{
				op = m.group(2);
			}
			
			if (m.find()) throw new DataInvalidException("JSON path invalid: more op.");
			String sizePath = path.replaceAll(regex1, "[size()]");
			Object leno = jsonValueInner(sizePath, jo);
			if ("nil".equals(leno) || leno == null)
			{
				if ("loop".equals(op) || "find".equals(op)) return new ArrayList<>();
				return "nil";
			}
			
			int lsize = (Integer) leno;
			
			List<Object> vl = new ArrayList<>();
			for (int i = 0; i < lsize ; i++)
			{
				String npath = path.replaceAll(regex1, "[" + i + "]");
				Object o = jsonValueInner(npath, jo);
				vl.add(o);
			}
			
			switch (op) {
				case "loop":
				case "find": 
					return vl;
				case "data":
					StringBuilder buff = new StringBuilder("@data:");
					for (Object o : vl)
					{
						buff.append(String.valueOf(o)).append(",");
					}
					if (buff.charAt(buff.length() - 1) == ',') buff.deleteCharAt(buff.length() - 1);
					return buff.toString();
				case "unique":
					List<Object> chkl = new ArrayList<Object>();
					for (Object o : vl)
					{
						if (chkl.contains(o)) return "false";
						chkl.add(o);
					}
					return "true";
				case "asc":
					List<String> svl3 = new ArrayList<>();
					for (int i = 0; i < vl.size(); i++)
					{
						svl3.add(String.valueOf(vl.get(i)));
					}
					List<String> svl4 = new ArrayList<>();
					svl4.addAll(svl3);
					Collections.sort(svl4);
					
					for (int i = 0; i < svl3.size(); i++)
					{
						if (svl3.get(i).equals(svl4.get(i)))
						{
							continue;
						}
						else
						{
							return "false";
						}
					}
					
					return "true";
				case "desc":
					List<String> svl = new ArrayList<>();
					for (int i = 0; i < vl.size(); i++)
					{
						svl.add(String.valueOf(vl.get(i)));
					}
					List<String> svl2 = new ArrayList<>();
					svl2.addAll(svl);
					Collections.sort(svl2);
					
					int len = svl.size();
					for (int i = 0; i < svl.size(); i++)
					{
						if (svl.get(i).equals(svl2.get(len - i - 1)))
						{
							continue;
						}
						else
						{
							return "false";
						}
					}
					
					return "true";
				default:
					return "nil";
			}
		}
		else
		{
			return jsonValueInner(path, jo);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object jsonValueInner(String path, Map<String, Object> jo) throws DataInvalidException
	{
		path = path.startsWith("json.") ? path.substring(5, path.length()) : path;
		String[] paths = split(path);
		
		Object curro = jo;
		for (String pvar : paths)
		{
			if (curro instanceof Map)
			{
				String selectors = pvar.indexOf('[') > 0 ? pvar.substring(pvar.indexOf('['), pvar.length()) : null;
				pvar = pvar.indexOf('[') > 0 ? pvar.substring(0, pvar.indexOf('[')) : pvar;
				
				if (!((Map) curro).containsKey(pvar))
				{
					return "nil";
				}
				
				curro = ((Map) curro).get(pvar);
				
				if (selectors != null /*&& curro instanceof List*/)
				{
					String[] ss = splitSelector(selectors);
					
					for (String selector : ss)
					{
						if ("size()".equals(selector))
						{
							return curro == null ? 0 : curro instanceof Collection<?> ?
									((Collection) curro).size() : curro instanceof Map ?
											((Map) curro).size() : String.valueOf(curro).length();
						}
						
						if (curro == null)
						{
							break;
						}
						
						if (curro instanceof Map)
						{
							curro = ((Map) curro).values();
						}
						
						Collection<?> ol = (Collection<?>) curro;
						
						if (selector.contains("="))
						{
							String selectVar[] = selector.split("=", 2);
							String key = selectVar[0].trim();
							String v = selectVar[1].trim();
							
							Object to = null;
							for (Object lo : ol)
							{
								if (lo instanceof Map)
								{
									Map<String, Object> mo = (Map<String, Object>) lo;
									if (v.equals(jsonValue(key, mo)))
									{
										to = mo;
										break;
									}
								}
							}
							
							curro = to == null ? "nil" : to;
						}
						else
						{
							int idx = 0;
							try {
								idx = Integer.valueOf(selector.trim());
							} catch (NumberFormatException e) {
								curro = "nil";
								continue;
							}
							
							curro = ol.size() > idx ? new ArrayList<>(ol).get(idx): "nil";
						}
					}// FOR EACH
				}
				else if (curro instanceof List) // selector is null
				{
					List<?> ol = (List<?>) curro;
					curro = ol.isEmpty() ? "nil" : ol.get(0);
				}
				else
				{
					if (pvar.equals(paths[paths.length - 1]))
					{
						return curro;
					}
				}
			}
			else // not map
			{
				if (path.equals(paths[paths.length - 1]))
				{
					return curro;
				}
				else
				{
					return "nil";
				}
			}
		}
		
		return curro;
	}
	
	
	public static String[] splitSelector(String selector) throws DataInvalidException
	{
		List<String> sl = new ArrayList<String>();
		StringBuilder buff = new StringBuilder();
		
		int in = 0;
		for (int i = 0; i < selector.length(); i++)
		{
			char c = selector.charAt(i);
			
			if (in > 0)
			{
			    if (c == '[')
			    {
			    	in++;
			    	buff.append(c);
			    }
			    else if (c == ']')
			    {
			    	in--;
			    	if (in == 0)
			    	{
			    		sl.add(buff.toString().trim());
			    		buff.delete(0, buff.length());
			    	}
			    	else
			    	{
			    		buff.append(c);
			    	}
			    }
			    else
			    {
			    	buff.append(c);
			    }
			}
			else
			{
				if (c == '[')
				{
					in++;
				}
				else
				{
					//invalid
					throw new DataInvalidException("Selector invalid.");
				}
			}
		}
		
		if (in != 0) throw new DataInvalidException("Selector invalid.");
		
		return sl.toArray(new String[sl.size()]);
	}
	
	
	/**
	 * json.rlt.list[0].value
	 * 
	 * @param path
	 * @return String[]
	 */
	public static String[] split(String path) throws DataInvalidException
	{
		List<String> paths = new ArrayList<String>();
		
		boolean needEnd = false;
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < path.length(); i++)
		{
			char c = path.charAt(i);
			if (c == '.')
			{
				if (needEnd)
				{
					buff.append(c);
					continue;
				}
				
				String p = trimJP(buff.toString().trim());
				if (p == null || p.isEmpty()) throw new DataInvalidException("Path invalid: " + path);
				paths.add(p);
				buff.delete(0, buff.length());
				continue;
			}
			else if (c == '[')
			{
				needEnd = true;
			}
			else if (c == ']')
			{
				needEnd = false;
			}
			buff.append(c);
		}
		
		if (needEnd) throw new DataInvalidException("Path invalid: " + path);
		
		String p = buff.toString().trim();
		if (p.isEmpty()) throw new DataInvalidException("Path invalid: " + path);
		paths.add(p);
		
		return paths.toArray(new String[paths.size()]);
	}
	
	private static String trimJP(String path)
	{
		if (path.charAt(0) == '[')
		{
			int idx = path.indexOf(']');
			if (idx == path.length() - 1)
			{
				return path.substring(1, path.length() - 1);
			}
			
			if (path.charAt(idx + 1) == '[' && path.charAt(path.length() - 1) == ']')
			{
				return path.substring(1, idx) + path.substring(idx + 1);
			}
			
			return null;
		}
		
		return path;
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, Object> xml2Json(Element e)
	{
		Map<String, Object> jo = new HashMap<String, Object>();
		List<?> attrl = e.getAttributes();
		for (Object attro : attrl)
		{
			Attribute attr = (Attribute) attro;
			jo.put("@" + attr.getName(), attr.getValue());
		}
		
		if (!e.getTextTrim().isEmpty())
		{
			jo.put("value", e.getTextTrim());
		}
		
		List<String> l = uniqueChildName(e);
		for (String cname : l)
		{
			List<?> sel = e.getChildren(cname);
			if (sel.size() == 1)
			{
				Element se = (Element) sel.get(0);
				jo.put(se.getName(), xml2Json(se));
			}
			else
			{
				List<Object> ol = new ArrayList<Object>();
				jo.put(cname, ol);
				for (Element se : (List<Element>) sel)
				{
					ol.add(xml2Json(se));
				}
			}
		}
		
		return jo;
	}
	
	private static List<String> uniqueChildName(Element e)
	{
		List<String> l = new ArrayList<String>();
		for (Object o : e.getChildren())
		{
			if (o instanceof Element)
			{
				Element se = (Element) o;
				if (!l.contains(se.getName()))
				{
					l.add(se.getName());
				}
			}
		}
		return l;
	}
	
	private static void delNS(Element e)
	{
		e.setNamespace(null);
		for (Object o : e.getChildren())
		{
			if (o instanceof Element)
			{
				Element se = (Element) o;
				delNS(se);
			}
		}
	}
	
}
