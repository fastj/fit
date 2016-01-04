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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fastj.fit.intf.ParameterTable;

/**
 * @author zhouqingquan
 *
 */
public final class TSuite implements org.fastj.fit.intf.TSuite{
	
	private static int priLevel = 0;
	
	private String name;
	private int level = -1;
	private TProject project;
	private ParameterTable paramTable = new ParameterTable();
	private List<TestCase> testCases = new ArrayList<TestCase>();
	
	public static TSuite create(String name) throws NumberFormatException
	{
		if (TProject.SETUP_FILE.equals(name))
		{
			return new TSuite(name, -1);
		}
		
		try {
			Matcher m = Pattern.compile(TProject.TS_NAME_REGEX).matcher(name);
			m.find();
			return new TSuite(name, Integer.valueOf(m.group(2)));
		} catch (Throwable e) {
			return new TSuite(name, priLevel++);
		}
	}
	
	public static TSuite createV2(String name, int l) throws NumberFormatException
	{
		return new TSuite(name, l);
	}
	
	private TSuite(String name, int l)
	{
		if (name == null || name.isEmpty())
		{
			throw new IllegalArgumentException("TSuite name is null.");
		}
		
		if (!TProject.SETUP_FILE.equals(name))
		if (l < 0 || l > 10000)
		{
			throw new IllegalArgumentException("TSuite level[0, 10000] is invalid: " + level);
		}
		
		this.name = name;
		this.level = l;
	}
	
	public void addTestCase(TestCase tc)
	{
		testCases.add(tc);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<TestCase> getTestCases() {
		return testCases;
	}
	
	public TestCase getLast()
	{
		return testCases.isEmpty() ? null : testCases.get(testCases.size() - 1);
	}

	public void addTestCases(List<TestCase> tcs) {
		if (tcs != null)
		{
			this.testCases.addAll(tcs);
		}
	}

	public ParameterTable getParamTable() {
		return paramTable;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
		{
			return false;
		}
		
		if (obj instanceof TSuite)
		{
			return obj.hashCode() == hashCode();
		}
		
		return false;
	}

	public TProject getProject() {
		return project;
	}

	public void setProject(TProject project) {
		this.project = project;
		paramTable.setParent(project.getSysVars());
		project.addSuite(this);
	}
	
}
