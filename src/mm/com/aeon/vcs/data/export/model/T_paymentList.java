package mm.com.aeon.vcs.data.export.model;

import java.io.Serializable;

public class T_paymentList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9206794035159606756L;

	private String agreementCd;

	private String paymentDate;

	public String getAgreementCd() {
		return agreementCd;
	}

	public void setAgreementCd(String agreementCd) {
		this.agreementCd = agreementCd;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

}
