package com.softfun_xmpp.bean;

import java.io.Serializable;

public class OrgBean implements Serializable {
	private String orgid;
	private String orgname;
	private String parentorg;
	
	public String getOrgid() {
		return orgid;
	}
	public void setOrgid(String orgid) {
		this.orgid = orgid;
	}
	public String getOrgname() {
		return orgname;
	}
	public void setOrgname(String orgname) {
		this.orgname = orgname;
	}
	public void setParentorg(String parentorg) {
		this.parentorg = parentorg;
	}
	public String getParentorg() {
		return parentorg;
	}
	
	
	
	
}
