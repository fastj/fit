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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class NodeLogger {
	
	private Object lock = new Object();
	private ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
	private PrintStream pstream = new PrintStream(bout);
	
	public String getLog()
	{
		synchronized (lock) {
			return bout.toString();
		}
	}
	
	public void clear() {
		synchronized (lock) {
			bout = new ByteArrayOutputStream(2);
		}
	}
	
	public void debug(String msg, Object ... args)
	{
		if (LogUtil.debug())
		{
			synchronized (lock) {
				LogUtil.log(pstream, "DEBUG", msg, args);
			}
		}
	}
	
	public void info(String msg, Object ... args)
	{
		if (LogUtil.info())
		{
			synchronized (lock) {
				LogUtil.log(pstream, "INFO", msg, args);
			}
		}
	}
	
	public void warn(String msg, Object ... args)
	{
		if (LogUtil.warn())
		{
			synchronized (lock) {
				LogUtil.log(pstream, "WARNING", msg, args);
			}
		}
	}
	
	public void trace(String msg, Object ... args)
	{
		if (LogUtil.trace())
		{
			synchronized (lock) {
				LogUtil.log(pstream, "TRACE", msg, args);
			}
		}
	}
	
	public void error(String msg, Object ... args)
	{
		if (LogUtil.error())
		{
			synchronized (lock) {
				LogUtil.log(pstream, "ERROR", msg, args);
			}
		}
	}
	
	public void error(String msg, Throwable t, Object ... args)
	{
		if (LogUtil.error())
		{
			synchronized (lock) {
				LogUtil.log(pstream, "ERROR", msg, args);
				t.printStackTrace(pstream);
			}
		}
	}
	
	public void append(String msg)
	{
		synchronized (lock) {
			pstream.println(msg);
		}
	}
	
	public void append(NodeLogger nlog)
	{
		synchronized (lock) {
			try {
				nlog.bout.writeTo(bout);
			} catch (IOException e) {
			}
		}
	}
}
