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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.fastj.fit.log.LogUtil;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * JdomHelper
 * 
 * @author zhouqingquan
 *
 */
public final class JdomHelper {
	
	/**
	 * 写XML文件
	 * @param doc   XML对象 JDOM
	 * @param file  文件名
	 */
	public static void write(Document doc,String file){
		
		File f = new File(file);
		if(f.exists()){
			f.delete();
		}	
		write(doc,f);
	}
	
	/**
	 * 写XML文件
	 * @param doc   XML对象 JDOM
	 * @param file  文件对象
	 */
	public static void write(Document doc,File file){
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		
		try (FileOutputStream fos = new FileOutputStream(file);){
			out.output(doc, fos);
		} catch (Throwable e) {
			LogUtil.error("Write doc fail : e={}", e.getMessage());
		}
	}
	
	/**
	 * 读XML文件
	 * @param file  文件名
	 * @return  Document对象
	 */
	public static Document read(String file){
		return read(new File(file));
	}
	
	/**
	 * 读XML文件
	 * @param file  文件对象
	 * @return  Document对象
	 */
	public static Document read(File file){
		try (FileInputStream fin = new FileInputStream(file);){
			return new SAXBuilder().build(fin);
		} catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * 读XML格式的字符串
	 * @param content     XML格式数据
	 * @param encoding    字符编码
	 * @return Document
	 */
	public static Document build(String content, String encoding){
		try (ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes(encoding));){
			return new SAXBuilder().build(bis);
		} catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * 读XML格式的字符串,默认编码格式UTF-8
	 * @param content     XML格式数据
	 * @return Document
	 */
	public static Document build(String content){
		return build(content, "utf-8");
	}
	
}
