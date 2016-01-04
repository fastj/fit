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

import java.util.List;

/**
 * 测试用例抽象接口
 * 
 * @author zhouqingquan
 *
 */
public interface TCNode {

	int PASS = 0;
	int FAIL = 1;
	int BLOCKED = 2;
	int SKIPPED = 4;
	int FAST_FAIL = 5;
	
	long getStartTime();
	
	long getEndTime();
	
	String getName();
	
	String getTid();
	
	TSuite getSuite();
	
	List<TResult> getResults();
	
	int getResult();
	
	String getLog();
}
