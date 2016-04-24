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

package org.fastj.fit.log;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	
	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int WARN = 2;
	public static final int ERROR = 3;
	public static final int TRACE = 10;
	public static final int CLOSE = 16;
	private static final String DFORMAT = "yyyy-MM-DD HH:mm:ss";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(DFORMAT);
	
	public static int level = INFO;
	
	static NodeLogger nlog = new NodeLogger();
	
	public static String date(){
		return sdf.format(new Date());
	}
	
	public static byte[] getLog()
	{
		return nlog.getLog().getBytes();
	}
	
	public static void setConsoleOut(OutputStream out){
		if (out == null) nlog.consoleOut = null;
		nlog.consoleOut = new PrintStream(out);
	}
	
	public static boolean debug()
	{
		return level <= DEBUG;
	}
	
	public static boolean info()
	{
		return level <= INFO;
	}
	
	public static boolean warn()
	{
		return level <= WARN;
	}
	
	public static boolean error()
	{
		return level <= ERROR;
	}
	
	public static boolean trace()
	{
		return level <= TRACE;
	}
	
	public static void closeLog()
	{
		level = CLOSE;
	}
	
	public static void debug(String msg, Object ... args)
	{
		nlog.debug(msg, args);
	}
	
	public static void info(String msg, Object ... args)
	{
		nlog.info(msg, args);
	}
	
	public static void warn(String msg, Object ... args)
	{
		nlog.warn(msg, args);
	}
	
	public static void trace(String msg, Object ... args)
	{
		nlog.trace(msg, args);
	}
	
	public static void error(String msg, Object ... args)
	{
		nlog.error(msg, args);
	}
	
	public static void error(String msg, Throwable t, Object ... args)
	{
		nlog.error(msg, t, args);
	}
	
	public static String format(String level, String expr, Object ... args)
	{
		if (args == null || args.length == 0)
		{
			return date() + "  " + level + "  " + String.valueOf(expr);
		}
		
		StringBuilder buff = new StringBuilder(1024);
		buff.append(date()).append("  ").append(level).append("  ").append(expr);
		int idx = -1;
		int tag = 0;
		
		while((idx = buff.indexOf("{}")) >= 0 && tag < args.length)
		{
			buff.replace(idx, idx + 2, String.valueOf(args[tag++]));
		}
		
		return buff.toString();
	}
	
}
