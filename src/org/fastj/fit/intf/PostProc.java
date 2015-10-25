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

import org.fastj.fit.intf.TProject;
import org.fastj.fit.jenkins.JenkinsPostProc;

/**
 * 执行过程中的反调接口，用于日志，报告等
 * 
 * @see {@link JenkinsPostProc}
 * 
 * @author zhouqingquan
 *
 */
public interface PostProc {
	
	void end();
	
	void start(TProject tproj);
	
	void finish(TCNode tcn);
	
	void finish(TSuite suite);
	
}
