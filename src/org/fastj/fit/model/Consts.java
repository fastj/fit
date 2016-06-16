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

package org.fastj.fit.model;

import org.fastj.fit.intf.TCNode;

/**
 * 关键字及常量定义 
 * 
 * @author zhouqingquan
 *
 */
public final class Consts {
	
	public static final int PASS = TCNode.PASS;
	public static final int FAIL = TCNode.FAIL;
	public static final int BLOCKED = TCNode.BLOCKED;
	public static final int SKIPPED = TCNode.SKIPPED;
	public static final int FAST_FAIL = TCNode.FAST_FAIL;
	
	public static final int TSTAGE_PRE = -1;
	public static final int TSTAGE_MAIN = 0;
	public static final int TSTAGE_TEARDOWN = 1;
	
	public static final String PATTERN_DATATABLE = "@data:";
	
	public static final String VERIFY_MODE_MATCH = "match"; //pattern match
	public static final String VERIFY_MODE_MATCH1 = "pt"; //pattern match
	public static final String VERIFY_MODE_NOT_MATCH = "!match"; //pattern match
	public static final String VERIFY_MODE_EQ = "="; // equals
	public static final String VERIFY_MODE_EQ1 = "=="; //Number ==
	public static final String VERIFY_MODE_GE = ">="; //Number >=
	public static final String VERIFY_MODE_LE = "<="; //Number <=
	public static final String VERIFY_MODE_GT = ">"; //Number >
	public static final String VERIFY_MODE_LT = "<"; //Number <
	public static final String VERIFY_MODE_NE = "!="; //Not equals
	public static final String VERIFY_MODE_IN = "in"; //in this list, ',' split line
	public static final String VERIFY_MODE_NIN = "!in"; //Not in this list
	public static final String VERIFY_MODE_CONTAINS = "ct"; //contains
	public static final String VERIFY_MODE_NOT_CONTAINS = "!ct"; //contains
	
	public static final String STEP_FUNC_PATTERRN = "^\\$(\\([\\S\\s]*\\))*[ ]*([A-Za-z0-9_]{1,})\\(([\\S\\s]*)\\)\\s*$";
	
	public static final String FUNC_PATTERRN = "^([A-Za-z0-9_]{1,})\\(([\\S\\s]*)\\)$";
	
	public static final String IMPORT_CONSTS_PATTERN = "^import[\\s]{1,}[\\S]*consts([\\S\\s]*)$";
	
	public static final String IMPORT_STC_PATTERN = "^import[\\s]{1,3}([\\S\\s]*)\\.fitc$";
	
	public static final String RUN_FILE_PATTERN = "";
	
	public static final String NUMBER_REGEX = "^[\\d]+(\\.[\\d]*)?$";
}
