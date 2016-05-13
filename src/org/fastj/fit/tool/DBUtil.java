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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastj.fit.intf.DataInvalidException;

/**
 * 数据库操作，结果为json结构
 * 
 * @author zhouqingquan
 *
 */
public class DBUtil {
	
	public static Map<String, Object> exec(String dbUrl, String user, String pass, String sql)
	{
		Connection c = null;
		try {
			c = getDbConn(dbUrl, user, pass);
		} catch (Throwable e) {
			HashMap<String, Object> rlt = new HashMap<>();
			rlt.put("code", 1);
			rlt.put("message", e.getClass().getName() + ": " + e.getMessage());
			return rlt;
		}
		
		if (c == null)
		{
			HashMap<String, Object> rlt = new HashMap<>();
			rlt.put("code", 1);
			rlt.put("message", "Get DB connection fail.");
			return rlt;
		}
		
		try (PreparedStatement ps =  c.prepareStatement(sql)) {
			
			if (sql.startsWith("select"))
			{
				ResultSet rs = ps.executeQuery();
				ResultSetMetaData rsmd = rs.getMetaData();
				
				int cols = rsmd.getColumnCount();
				List<String> colNs = new ArrayList<String>();
				while(cols > 0) colNs.add(rsmd.getColumnLabel(cols--));
				
				List<Object> rl = new ArrayList<Object>();
				while(rs.next())
				{
					Map<String, Object> r = new HashMap<String, Object>();
					for (String cl : colNs)
					{
						r.put(cl, rs.getString(cl));
					}
					rl.add(r);
				}
				Map<String, Object> rlt = new HashMap<String, Object>();
				rlt.put("code", 0);
				rlt.put("records", rl);
				return rlt;
			}
			else
			{
				int cnt = ps.executeUpdate();
				HashMap<String, Object> rlt = new HashMap<>();
				rlt.put("code", 0);
				rlt.put("count", cnt);
				return rlt;
			}
		} catch (Throwable e) {
			HashMap<String, Object> rlt = new HashMap<>();
			rlt.put("code", 2);
			rlt.put("message", e.getClass().getName() + ":" + e.getMessage());
			return rlt;
		}
		finally
		{
			releaseConn(c);
		}
	}
	
	public static String execSql(String dbUrl, String user, String pass, String sql)
	{
		return JSONHelper.jsonString(exec(dbUrl, user, pass, sql));
	}
	
	private static Connection getDbConn(String dbUrl, String user, String pass ) throws Exception
	{
		initDbDriver(dbUrl);
		return DriverManager.getConnection(dbUrl, user, pass);
	}
	
	private static void releaseConn(Connection c)
	{
		try {
			c.close();
		} catch (Throwable e) {
		}
	}
	
	public static void initDbDriver(String dburl) throws DataInvalidException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
	{
		String clazz = null;
		if (dburl.startsWith("jdbc:microsoft:sqlserver://"))
		{
			clazz = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
		}
		else if (dburl.startsWith("jdbc:sqlserver://"))
		{
			clazz = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		}
		else if (dburl.startsWith("jdbc:oracle:thin"))
		{
			clazz = "oracle.jdbc.driver.OracleDriver";
		}
		else if (dburl.startsWith("jdbc:db2:"))
		{
			clazz = "com.ibm.db2.jcc.DB2Driver";
		}
		else if (dburl.startsWith("jdbc:mysql://"))
		{
			clazz = "com.mysql.jdbc.Driver";
		}
		else if (dburl.startsWith("jdbc:informix-sqli://"))
		{
			clazz = "com.informix.jdbc.IfxDriver";
		}
		else if (dburl.startsWith("jdbc:sybase:Tds"))
		{
			clazz = "com.sybase.jdbc3.jdbc.SybDriver";
		}
		else if (dburl.startsWith("jdbc:postgresql://"))
		{
			clazz = "org.postgresql.Driver";
		}
		else if (dburl.startsWith("jdbc:teradata"))
		{
			clazz = "com.ncr.teradata.TeraDriver";
		}
		else if (dburl.startsWith("jdbc:netezza"))
		{
			clazz = "org.netezza.Driver";
		}
			
		if (clazz == null) throw new DataInvalidException("Unkown DB: " + dburl);
		
		Class<?> c = Class.forName(clazz, true, TSysInit.fitExtLoader);
		
		DriverManager.registerDriver((Driver)c.newInstance());
	}
	
}
