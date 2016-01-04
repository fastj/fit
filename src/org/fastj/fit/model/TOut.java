package org.fastj.fit.model;

public class TOut {
	
	public TOut(String nameExpr, String vExpr, boolean g){
		this.nameExpr = nameExpr;
		this.valueExpr = vExpr;
		this.global = g;
	}
	
	public String nameExpr;
	
	public String valueExpr;
	
	public boolean global = true;
}
