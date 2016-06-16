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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fastj.fit.tool.EFormat;

public class LogUtil {

	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int WARN = 2;
	public static final int ERROR = 3;
	public static final int TRACE = 10;
	public static final int CLOSE = 16;
	private static final String DFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(DFORMAT);
	private static PrintStream logholder = null;
	private static OutputStream console = System.out;
	private static OutputStream flog = null;

	public static int level = INFO;
	
	static {
		OutputStream LogOut = new  OutputStream() {

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				if (flog != null) {
					flog.write(b, off, len);
				}
				if (console != null) {
					console.write(b, off, len);
				}
			}

			@Override
			public void write(int b) throws IOException {
				if (flog != null) {
					flog.write(b);
				}
				if (console != null) {
					console.write(b);
				}
			}
			
		};
		
		logholder = new PrintStream(LogOut, true);
	}

	public static String date() {
		return sdf.format(new Date());
	}

	public static void setLogOut(OutputStream out) {
		flog = out;
	}

	public static void setConsoleOut(OutputStream out) {
		console = out;
	}

	public static boolean debug() {
		return level <= DEBUG;
	}

	public static boolean info() {
		return level <= INFO;
	}

	public static boolean warn() {
		return level <= WARN;
	}

	public static boolean error() {
		return level <= ERROR;
	}

	public static boolean trace() {
		return level <= TRACE;
	}

	public static void closeLog() {
		level = CLOSE;
	}

	public static void debug(String msg, Object... args) {
		if (LogUtil.debug()) {
			log("DEBUG", msg, args);
		}
	}

	public static void info(String msg, Object... args) {
		if (LogUtil.info()) {
			log("INFO", msg, args);
		}
	}

	public static void warn(String msg, Object... args) {
		if (LogUtil.warn()) {
			log("WARNING", msg, args);
		}
	}

	public static void trace(String msg, Object... args) {
		if (LogUtil.trace()) {
			log("TRACE", msg, args);
		}
	}

	public static void error(String msg, Object... args) {
		if (LogUtil.error()) {
			log("ERROR", msg, args);
		}
	}

	public static void error(String msg, Throwable t, Object... args) {
		if (LogUtil.error()) {
			log("ERROR", msg, args);
			log("ERROR", EFormat.exStrEx(t, true));
		}
	}

	private static void log(String level, String expr, Object... args) {
		log(logholder, level, expr, args);
	}
	
	public static void log(PrintStream ps, String level, String expr, Object... args) {
		try {
			log0(ps, level, expr, args);
		} catch (Throwable e) {
			System.err.println("Log fail: " + e.getMessage());
		}
	}
	
	public static void log0(PrintStream ps, String level, String expr, Object... args) {
		if (ps == null) return ;
		
		ps.print(date());
		ps.print("  ");
		ps.print(level);
		ps.print("  ");

		if (args == null || args.length == 0) {
			ps.print(String.valueOf(expr));
			ps.println();
			ps.flush();
			return;
		}

		int idx = -1, pidx = 0;
		int tag = 0;

		while ((idx = expr.indexOf("{}", pidx)) >= 0 && tag < args.length) {
			ps.append(expr, pidx, idx);
			pidx = idx + 2;
			ps.print(String.valueOf(args[tag++]));
		}

		if (pidx < expr.length() - 1) {
			ps.append(expr, pidx, expr.length());
		}

		ps.println();
		ps.flush();
	}

//	public static String format(String level, String expr, Object... args) {
//		if (args == null || args.length == 0) {
//			return date() + "  " + level + "  " + expr;
//		}
//
//		StringBuilder buff = new StringBuilder(1024);
//		buff.append(date()).append("  ").append(level).append("  ").append(expr);
//		int idx = -1;
//		int tag = 0;
//
//		while ((idx = buff.indexOf("{}")) >= 0 && tag < args.length) {
//			buff.replace(idx, idx + 2, String.valueOf(args[tag++]));
//		}
//
//		return buff.toString();
//	}

	
}
