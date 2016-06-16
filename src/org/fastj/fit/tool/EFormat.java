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

/**
 * @author zhouqingquan
 *
 */
public class EFormat {
	
	public static String exStrEx(Throwable t, boolean ex)
	{
		StringBuilder buff = new StringBuilder();
		
		boolean first = true;
		do {
			if (first)
			{
				buff.append(t.getClass().getName()).append(":").append(t.getMessage()).append("\r\n");
				first = false;
			}
			else
			{
				buff.append("Cased by ").append(t.getClass().getName()).append(":").append(t.getMessage()).append("\r\n");
			}
			StackTraceElement astacktraceelement[] = t.getStackTrace();
			for (int i = 0; i < astacktraceelement.length; i++) {
				String stack = astacktraceelement[i].toString();
				if (ex)
				{
					buff.append("\tat ").append(stack).append("\n");
				}
				else
				{
					if (stack.startsWith("java") || stack.startsWith("sun")) continue;
					if (stack.startsWith("org.fastj")){
						buff.append("\tat ").append(stack).append("\n");
					}else
						buff.append("\tat ").append(stack).append("\n");
				}
			}
		}while ((t = t.getCause()) != null);
		return buff.toString();
	}
	
	public static String exStr(Throwable t)
	{
		return exStrEx(t, false);
	}
	
	public static String getString(String msg, Throwable t) {
		StringBuilder sb = new StringBuilder(msg == null || "".equals(msg) ? "" : msg + " : ");
		sb.append(t.toString()).append("\n");
		StackTraceElement astacktraceelement[] = t.getStackTrace();
		for (int i = 0; i < astacktraceelement.length; i++)
			sb.append((new StringBuilder()).append("\tat ").append(astacktraceelement[i]).toString()).append("\n");

		return sb.toString();
	}
	
}
