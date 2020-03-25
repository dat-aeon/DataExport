package mm.com.aeon.vcs.data.export.model;

/**************************************************************************
 * $Date : $
 * $Author : $
 * $Rev : $
 * Copyright (c) 2014 DIR-ACE Technology Ltd. All Rights Reserved.
 *************************************************************************/

import java.io.Serializable;
import java.util.List;

public class ImportCustomerInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5291122098561544640L;

	private String companyName;
	private String customerNo;
	private String delFlag;
	private String dob;
	private String gender;
	private String memberCardId;
	private String memberCardStatus;
	private String name;
	private String nrcNo;
	private String phoneNo;
	private String salary;
	private String township;

	private List<CustAgreementList> custAgreementListList;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(String delFlag) {
		this.delFlag = delFlag;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getMemberCardId() {
		return memberCardId;
	}

	public void setMemberCardId(String memberCardId) {
		this.memberCardId = memberCardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNrcNo() {
		return nrcNo;
	}

	public void setNrcNo(String nrcNo) {
		this.nrcNo = nrcNo;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public String getTownship() {
		return township;
	}

	public void setTownship(String township) {
		this.township = township;
	}

	public String getMemberCardStatus() {
		return memberCardStatus;
	}

	public void setMemberCardStatus(String memberCardStatus) {
		this.memberCardStatus = memberCardStatus;
	}

	public List<CustAgreementList> getCustAgreementListList() {
		return custAgreementListList;
	}

	public void setCustAgreementListList(List<CustAgreementList> custAgreementListList) {
		this.custAgreementListList = custAgreementListList;
	}

}
