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

package org.fastj.fit.intf;

/**
 * 实现该接口可以扩展FIT
 * 
 * $callName(...)
 * 
 * @author zhouqingquan
 *
 */
public interface IFuncCall {
	
	/**
	 * Call name
	 * 
	 * @return String
	 */
	String name();
	
	/**
	 * 功能函数
	 * 
	 * @param ctx       用例运行上下文， 用例可复制时， 每个用例都有自己的上下文（独立）
	 * @param table     系统变量表  System variables Table
	 * @param argStr    调用参数
	 * @return  FuncResponse
	 * @throws ParamIncertitudeException
	 * @throws DataInvalidException
	 */
	FuncResponse run(TContext ctx, ParameterTable table, String argStr) throws ParamIncertitudeException, DataInvalidException;
}
