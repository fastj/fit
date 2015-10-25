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

import java.io.File;

/**
 * 测试工程
 * 
 * @author zhouqingquan
 *
 */
public interface TProject {
	
	String CONSTS_FILE = "consts.script";
	String NAME_REGEX = "^([\\S\\s]*)__L([0-9]{1,})\\.script$";
	String AW_NAME_REGEX = "^([\\S\\s]*)\\.aws$";
	
	String RESOURCES_DIR = "resources";
	String RUNDATA_DIR = "data";
	String LOG_DIR = "logs";
	
	String getDir();
	
	ParameterTable getSysVars();
	
	File getProjectFile(String file);
	
	File getResourceFile(String fileName);
	
	File getLogFile(String fileName);
	
	File getDataFile(String fileName);
}
