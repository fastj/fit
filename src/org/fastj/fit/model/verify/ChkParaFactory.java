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

package org.fastj.fit.model.verify;

import org.fastj.fit.intf.DataInvalidException;
import org.fastj.fit.model.Consts;

public class ChkParaFactory {
	
	public static ChkPara get(String key, String op, String expValue) throws DataInvalidException
	{
		if (Consts.VERIFY_MODE_MATCH.equals(op) || Consts.VERIFY_MODE_MATCH1.equals(op))
		{
			return new PatternPara(key, Consts.VERIFY_MODE_MATCH, expValue);
		}
		else if (Consts.VERIFY_MODE_NIN.startsWith(op))
		{
			return new NINPara(key, expValue);
		}
		else if (Consts.VERIFY_MODE_IN.startsWith(op))
		{
			return new INPara(key, expValue);
		}
		else if (Consts.VERIFY_MODE_CONTAINS.equals(op))
		{
			return new ContainsPara(key, op, expValue);
		}
		else if (Consts.VERIFY_MODE_NOT_CONTAINS.equals(op))
		{
			return new ContainsPara(key, op, expValue).setNot();
		}
		else if (op.matches("^(=|==|!=|>=|>|<|<=)$"))
		{
			return new DefaultPara(key, op, expValue);
		}
		
		throw new DataInvalidException("Unsupport op: " + op);
	}
	
}
