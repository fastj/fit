package org.fastj.fit.intf;

public class Response<T> {
	
	public static final int OK = 0x0;
	
	public static final int UNKOWN_ERROR = 1000000;
	
	//return code
	private int code = OK;
	
	//response body or fail reason 
	private String phrase = null;
	
	private long reqId;
	
	private T entity;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public long getReqId() {
		return reqId;
	}

	public void setReqId(long reqId) {
		this.reqId = reqId;
	}
	
	public T getEntity()
	{
		return entity;
	}
	
	public void setEntity(T vbs)
	{
		this.entity = vbs;
	}
	
	public String toString()
	{
		return "";
	}
}
