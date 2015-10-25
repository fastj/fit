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

import org.fastj.fit.tool.EFormat;

public class NodeLogger {
	
	private StringBuilder logBuffer = new StringBuilder();
	
	public String getLog()
	{
		return logBuffer.toString();
	}
	
	public void debug(String msg, Object ... args)
	{
		if (LogUtil.debug())
		{
			log(LogUtil.format(msg, args));
		}
	}
	
	public void info(String msg, Object ... args)
	{
		if (LogUtil.info())
		{
			log(LogUtil.format(msg, args));
		}
	}
	
	public void warn(String msg, Object ... args)
	{
		if (LogUtil.warn())
		{
			log(LogUtil.format(msg, args));
		}
	}
	
	public void trace(String msg, Object ... args)
	{
		if (LogUtil.trace())
		{
			log(LogUtil.format(msg, args));
		}
	}
	
	public void error(String msg, Object ... args)
	{
		if (LogUtil.error())
		{
			log(LogUtil.format(msg, args));
		}
	}
	
	public void error(String msg, Throwable t, Object ... args)
	{
		if (LogUtil.error())
		{
			log(LogUtil.format(msg, args) + "\r\n    " + EFormat.exStr(t));
		}
	}
	
	public void append(String msg)
	{
		logBuffer.append(msg).append("\r\n");
	}
	
	public void append(NodeLogger nlog)
	{
		logBuffer.append(nlog.logBuffer).append("\r\n");
	}
	
	private void log(String msg)
	{
		logBuffer.append(msg).append("\r\n");
		System.out.println(msg);
	}
}
