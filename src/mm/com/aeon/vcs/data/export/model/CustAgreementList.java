package mm.com.aeon.vcs.data.export.model;

import java.io.Serializable;

public class CustAgreementList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -122807271358951412L;

	private String agreementNo;
	private String qrShow;
	private String financeTerm;
	private String financeAmount;
	private String financialStatus;
	private String applicationFormNo;
	private String JudgementDate;
	private String JudgementResult;
	private String paymentDate;

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getQrShow() {
		return qrShow;
	}

	public void setQrShow(String qrShow) {
		this.qrShow = qrShow;
	}

	public String getAgreementNo() {
		return agreementNo;
	}

	public void setAgreementNo(String agreementNo) {
		this.agreementNo = agreementNo;
	}

	public String getFinanceTerm() {
		return financeTerm;
	}

	public void setFinanceTerm(String financeTerm) {
		this.financeTerm = financeTerm;
	}

	public String getFinanceAmount() {
		return financeAmount;
	}

	public void setFinanceAmount(String financeAmount) {
		this.financeAmount = financeAmount;
	}

	public String getFinancialStatus() {
		return financialStatus;
	}

	public void setFinancialStatus(String financialStatus) {
		this.financialStatus = financialStatus;
	}

	public String getApplicationFormNo() {
		return applicationFormNo;
	}

	public void setApplicationFormNo(String applicationFormNo) {
		this.applicationFormNo = applicationFormNo;
	}

	public String getJudgementDate() {
		return JudgementDate;
	}

	public void setJudgementDate(String judgementDate) {
		JudgementDate = judgementDate;
	}

	public String getJudgementResult() {
		return JudgementResult;
	}

	public void setJudgementResult(String judgementResult) {
		JudgementResult = judgementResult;
	}

}
