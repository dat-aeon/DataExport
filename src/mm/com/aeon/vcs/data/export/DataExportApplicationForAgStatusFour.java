package mm.com.aeon.vcs.data.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.opencsv.CSVWriter;

import mm.com.aeon.vcs.data.export.common.CommonConstant;
import mm.com.aeon.vcs.data.export.common.CommonUtils;
import mm.com.aeon.vcs.data.export.common.MessageConstant;
import mm.com.aeon.vcs.data.export.model.CustAgreementList;
import mm.com.aeon.vcs.data.export.model.ImportCustomerInfo;

public class DataExportApplicationForAgStatusFour {

	private static boolean printStackTrace;

	public static String JAR_DIR = "/";

	private static Properties properties;

	private static int maxAgreementCount = 0;

	private static int totalRecords = 0;

	private static String lastExecutedTime_yyyymmddhhmmss_string = "";

	private static String executionStartTime_yyyymmddhhmmss_string = "";

	private static String WHERE_CLAUSE = "";

	private static int recordsPerEachFile = 10000;

	private static String AGREEMENT_STATUS = "";

	public static void main(String[] args) {

		JAR_DIR = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()).getAbsolutePath();

		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream(CommonUtils.getLogFileName(JAR_DIR)));
			System.setOut(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		executionStartTime_yyyymmddhhmmss_string = CommonUtils.getExectutionStartTimeString();

		CommonUtils.log(MessageConstant.PROCESS_START);

		List<String> customerNoList = new ArrayList<>();
		Connection connection = null;
		Connection agConn = null;

		if (!isPropertiesFileExist()) {
			CommonUtils.log(MessageConstant.PROCESS_END);
			return;
		}

		if (!isValidProperties()) {
			CommonUtils.log(MessageConstant.PROCESS_END);
			return;
		}

		AGREEMENT_STATUS = properties.getProperty(CommonConstant.AGREEMENT_STATUS);

		connection = getConnection();
		agConn = getConnection();

		if (connection == null) {
			CommonUtils.log(MessageConstant.PROCESS_END);
			return;
		}

		checkLastExecutedInfo();

		int rowCount = 0;
		Writer writer = null;
		CSVWriter csvWriter = null;

		Statement statement = null;
		ResultSet rs = null;

		Statement agStatement = null;
		ResultSet agRs = null;

		try {

			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = null;

			agStatement = agConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			// Get total data count
			rs = statement.executeQuery(getCustomerCountQueryString());
			if (rs.next()) {
				totalRecords = rs.getInt(CommonConstant.RECORD_COUNT);
			}

			CommonUtils.log("[Total Records : " + totalRecords + "]");

			if (totalRecords < 1) {
				connection.close();
				CommonUtils.log(MessageConstant.NO_DATA_TO_EXPORT);
				CommonUtils.log(MessageConstant.PROCESS_FINISH);
				return;
			}

			// check max agreement count
			rs = statement.executeQuery(getMaxAgreementCountQueryString());
			if (rs.next()) {
				maxAgreementCount = rs.getInt(CommonConstant.MAX);
			}

			rs = statement.executeQuery(getMainQueryString());

			ImportCustomerInfo customerInfo = null;
			CustAgreementList agreementList = null;

			while (rs.next()) {

				if (!customerNoList.contains(rs.getString(CommonConstant.CUSTOMER_NO).trim())) {

					customerInfo = new ImportCustomerInfo();

					if (rs.getString(CommonConstant.CUSTOMER_NO) == null) {
						CommonUtils.invalidProperty(CommonConstant.CUSTOMER_NO, "");
						continue;
					}
					customerInfo.setCustomerNo(rs.getString(CommonConstant.CUSTOMER_NO).trim());

					if (rs.getString(CommonConstant.CUSTOMER_NAME) == null) {
						CommonUtils.invalidProperty(CommonConstant.CUSTOMER_NAME, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setName(rs.getString(CommonConstant.CUSTOMER_NAME).trim());

					if (rs.getString(CommonConstant.GENDER) == null) {
						CommonUtils.invalidProperty(CommonConstant.GENDER, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setGender(rs.getString(CommonConstant.GENDER).trim());

					if (rs.getString(CommonConstant.PHONE_NO) == null) {
						CommonUtils.invalidProperty(CommonConstant.PHONE_NO, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setPhoneNo(rs.getString(CommonConstant.PHONE_NO).trim());

					if (rs.getString(CommonConstant.NRC) == null) {
						CommonUtils.invalidProperty(CommonConstant.NRC, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setNrcNo(rs.getString(CommonConstant.NRC).trim());

					if (rs.getString(CommonConstant.DOB) == null) {
						CommonUtils.invalidProperty(CommonConstant.DOB, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setDob(rs.getString(CommonConstant.DOB).trim());

					if (rs.getString(CommonConstant.SALARY) == null) {
						CommonUtils.invalidProperty(CommonConstant.SALARY, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setSalary(rs.getString(CommonConstant.SALARY).trim());

					if (rs.getString(CommonConstant.DEL_FLAG) == null) {
						CommonUtils.invalidProperty(CommonConstant.DEL_FLAG, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setDelFlag(rs.getString(CommonConstant.DEL_FLAG).trim());

					if (rs.getString(CommonConstant.COMPANY_NAME) == null) {
						CommonUtils.invalidProperty(CommonConstant.COMPANY_NAME, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setCompanyName(rs.getString(CommonConstant.COMPANY_NAME).trim());

					if (rs.getString(CommonConstant.TOWNSHIP_ADDRESS) == null) {
						CommonUtils.invalidProperty(CommonConstant.TOWNSHIP_ADDRESS, customerInfo.getCustomerNo());
						continue;
					}
					customerInfo.setTownship(rs.getString(CommonConstant.TOWNSHIP_ADDRESS).trim());

					if (rs.getString(CommonConstant.MEMBERCARD_ID) != null) {
						customerInfo.setMemberCardId(rs.getString(CommonConstant.MEMBERCARD_ID).trim());
					}

					// customerInfoList.add(customerInfo);
					customerNoList.add(rs.getString(CommonConstant.CUSTOMER_NO).trim());

					if (customerInfo.getCustAgreementListList() == null) {
						customerInfo.setCustAgreementListList(new ArrayList<>());
					}

					agRs = agStatement.executeQuery(getAgreementQueryString(customerInfo.getCustomerNo()));

					while (agRs.next()) {

						if (checkAgreement(agRs, customerInfo.getCustomerNo())) {

							agreementList = new CustAgreementList();

							agreementList.setAgreementNo(agRs.getString(CommonConstant.AGREEMENT_NO).trim());
							agreementList.setQrShow("1");
							agreementList.setFinanceAmount(agRs.getString(CommonConstant.FINANCIAL_AMT).trim());
							agreementList.setFinanceTerm(agRs.getString(CommonConstant.FINANCIAL_TERM).trim());
							agreementList.setFinancialStatus(agRs.getString(CommonConstant.FINANCIAL_STATUS).trim());

							customerInfo.getCustAgreementListList().add(agreementList);

						} else {
							customerInfo = null;
						}

					}

					if (agRs != null) {
						agRs.close();
					}

					if (rowCount % recordsPerEachFile == 0) {

						if (csvWriter != null) {
							csvWriter.flush();
							csvWriter.close();
							csvWriter = null;
						}

						if (csvWriter == null) {
							writer = Files.newBufferedWriter(Paths.get(generateFileName()));
						}

						csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
								CSVWriter.RFC4180_LINE_END);
						csvWriter.writeNext(getDefaultHeadingList().toArray(new String[0]));

					}

					if (customerInfo != null) {
						csvWriter.writeNext(getDataAsList(customerInfo).toArray(new String[0]));
						rowCount += 1;
					}

				}
			}

		} catch (SQLException e) {

			CommonUtils.log(MessageConstant.SQL_ERROR);

			printStackTrace(e);

		} catch (IOException e) {

			CommonUtils.log(MessageConstant.ERROR_CSV_FILE_CREATION);

			printStackTrace(e);

		}

		if (null != csvWriter) {
			try {
				csvWriter.flush();
				csvWriter.close();
			} catch (IOException e) {
				printStackTrace(e);
			}
		}

		if (null != agRs) {
			try {
				agRs.close();
			} catch (SQLException e) {
				printStackTrace(e);
			}
		}

		if (null != agStatement) {
			try {
				agStatement.close();
			} catch (SQLException e) {
				printStackTrace(e);
			}
		}

		if (null != rs) {
			try {
				rs.close();
			} catch (SQLException e) {
				printStackTrace(e);
			}
		}

		if (null != statement) {
			try {
				statement.close();
			} catch (SQLException e) {
				printStackTrace(e);
			}
		}

		if (null != connection) {
			try {
				connection.close();
			} catch (SQLException e) {
				printStackTrace(e);
			}
		}

		saveLastExecutedInfo();

		CommonUtils.log(MessageConstant.PROCESS_FINISH);
	}

	private static Connection getConnection() {
		Connection conn = null;

		try {

			conn = DriverManager.getConnection(properties.getProperty(CommonConstant.DATASOURCE_URL), properties.getProperty(CommonConstant.DATASOURCE_USERNAME),
					properties.getProperty(CommonConstant.DATASOURCE_PASSWORD));

		} catch (SQLException e) {

			CommonUtils.log(MessageConstant.DB_CONNECTION_ERROR);
			printStackTrace(e);

		}

		return conn;
	}

	private static boolean checkAgreement(ResultSet rs, String customerNo) {
		boolean isValid = true;

		try {
			if (rs.getString(CommonConstant.AGREEMENT_NO) == null) {
				CommonUtils.invalidProperty(CommonConstant.AGREEMENT_NO, customerNo);
				isValid = false;
			}

			if (rs.getString(CommonConstant.FINANCIAL_AMT) == null) {
				CommonUtils.invalidProperty(CommonConstant.FINANCIAL_AMT, customerNo);
				isValid = false;
			}

			if (rs.getString(CommonConstant.FINANCIAL_TERM) == null) {
				CommonUtils.invalidProperty(CommonConstant.FINANCIAL_TERM, customerNo);
				isValid = false;
			}

			if (rs.getString(CommonConstant.FINANCIAL_STATUS) == null) {
				CommonUtils.invalidProperty(CommonConstant.FINANCIAL_STATUS, customerNo);
				isValid = false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			isValid = false;
		}

		return isValid;
	}

	private static String getCustomerCountQueryString() {
		return "SELECT COUNT(CUS.count) AS recordcount FROM ( SELECT COUNT(ta.agreementcd) from aeon.m_customer c left join aeon.m_expresscard e on c.customercd = e.customercd left join aeon.t_agreement ta on c.customercd = ta.customercd left join aeon.t_paystagesagreement p on ta.agreementcd = p.agreementcd where p.financeprice > 0 and ta.agreementstatus IN"
				+ "(" + AGREEMENT_STATUS + ")" + WHERE_CLAUSE + "GROUP BY c.customercd )CUS";
	}

	private static String getMaxAgreementCountQueryString() {
		return "SELECT max(AG.count)" + " FROM ( SELECT COUNT(a.agreementcd)"
				+ " FROM aeon.m_customer c LEFT JOIN aeon.t_agreement a ON c.customercd = a.customercd left join aeon.t_paystagesagreement p on a.agreementcd = p.agreementcd where p.financeprice > 0 and a.agreementstatus IN"
				+ "(" + AGREEMENT_STATUS + ")" + WHERE_CLAUSE + "	GROUP BY c.customercd" + "	) AG";
	}

	// private static String getQueryString() {
	// return "select " + "c.customercd AS " + CommonConstant.CUSTOMER_NO + ",
	// c.name AS "
	// + CommonConstant.CUSTOMER_NAME + ", c.sex AS " + CommonConstant.GENDER +
	// ", c.mobileno AS "
	// + CommonConstant.PHONE_NO + ", c.idcardno AS " + CommonConstant.NRC + ",
	// c.birthday AS "
	// + CommonConstant.DOB + ", c.salary AS " + CommonConstant.SALARY + ",
	// c.delflag AS "
	// + CommonConstant.DEL_FLAG + ", c.corpname AS " +
	// CommonConstant.COMPANY_NAME + ", c.town AS "
	// + CommonConstant.TOWNSHIP_ADDRESS + ", e.expresscardno AS " +
	// CommonConstant.MEMBERCARD_ID
	// + ", a.agreementcd AS " + CommonConstant.AGREEMENT_NO + ", p.financeprice
	// AS "
	// + CommonConstant.FINANCIAL_AMT + ", p.numberofinstalment AS " +
	// CommonConstant.FINANCIAL_TERM
	// + ", a.agreementstatus AS " + CommonConstant.FINANCIAL_STATUS
	// + " from m_customer c left join m_expresscard e on c.customercd =
	// e.customercd"
	// + " left join t_agreement a on c.customercd = a.customercd"
	// + " left join t_paystagesagreement p on a.agreementcd = p.agreementcd" +
	// WHERE_CLAUSE
	// + " order by c.customercd";
	// }

	private static String getMainQueryString() {
		return "select " + "c.customercd AS " + CommonConstant.CUSTOMER_NO + ", c.name AS " + CommonConstant.CUSTOMER_NAME + ", c.sex AS " + CommonConstant.GENDER
				+ ", c.mobileno AS " + CommonConstant.PHONE_NO + ", c.idcardno AS " + CommonConstant.NRC + ", c.birthday AS " + CommonConstant.DOB + ", c.salary AS "
				+ CommonConstant.SALARY + ", c.delflag AS " + CommonConstant.DEL_FLAG + ", c.corpname AS " + CommonConstant.COMPANY_NAME + ", c.town AS "
				+ CommonConstant.TOWNSHIP_ADDRESS + ", e.expresscardno AS " + CommonConstant.MEMBERCARD_ID
				+ " from aeon.m_customer c left join aeon.m_expresscard e on c.customercd = e.customercd left join aeon.t_agreement ta on c.customercd = ta.customercd left join aeon.t_paystagesagreement p on ta.agreementcd = p.agreementcd where p.financeprice > 0 and ta.agreementstatus IN"
				+ "(" + AGREEMENT_STATUS + ")" + WHERE_CLAUSE + " order by c.customercd";
	}

	private static String getAgreementQueryString(String customerCd) {
		return "select a.agreementcd AS " + CommonConstant.AGREEMENT_NO + ", p.financeprice AS " + CommonConstant.FINANCIAL_AMT + ", p.numberofinstalment AS "
				+ CommonConstant.FINANCIAL_TERM + ", a.agreementstatus AS " + CommonConstant.FINANCIAL_STATUS
				+ " from aeon.t_agreement a left join aeon.t_paystagesagreement p on a.agreementcd = p.agreementcd" + " where p.financeprice > 0 and a.agreementstatus IN" + "("
				+ AGREEMENT_STATUS + ")" + " and a.customercd = '" + customerCd + "'" + " order by a.customercd";
	}

	private static boolean isValidProperties() {

		boolean valid = true;

		if (properties.isEmpty()) {

			CommonUtils.log(MessageConstant.PROPERTY_FILE_EMPTY);
			return false;

		}

		if (!properties.containsKey(CommonConstant.CSV_DEST_DIR)) {
			CommonUtils.log(MessageConstant.CSV_DEST_DIR_NOT_DEFINE);
			valid = false;
		} else if (CommonUtils.isNullOrTrimedEmpty(properties.get(CommonConstant.CSV_DEST_DIR))) {
			valid = false;
			CommonUtils.log(MessageConstant.CSV_DEST_DIR_IS_EMPTY);
		} else if (CommonUtils.dirNotExists(properties.getProperty(CommonConstant.CSV_DEST_DIR), false)) {
			valid = false;
			CommonUtils.log(MessageConstant.CSV_DEST_DIR_NOT_EXISTS);
		}

		if (!properties.containsKey(CommonConstant.DATASOURCE_URL)) {
			CommonUtils.log(MessageConstant.DATASOURCE_URL_NOT_DEFINE);
			valid = false;
		} else if (CommonUtils.isNullOrTrimedEmpty(properties.get(CommonConstant.DATASOURCE_URL))) {
			valid = false;
			CommonUtils.log(MessageConstant.DATASOURCE_URL_IS_EMPTY);
		}

		if (!properties.containsKey(CommonConstant.DATASOURCE_PASSWORD)) {
			CommonUtils.log(MessageConstant.DATASOURCE_PASSWORD_NOT_DEFINE);
			valid = false;
		} else if (CommonUtils.isNullOrTrimedEmpty(properties.get(CommonConstant.DATASOURCE_PASSWORD))) {
			valid = false;
			CommonUtils.log(MessageConstant.DATASOURCE_PASSWORD_IS_EMPTY);
		}

		if (!properties.containsKey(CommonConstant.DATASOURCE_USERNAME)) {
			CommonUtils.log(MessageConstant.DATASOURCE_USERNAME_NOT_DEFINE);
			valid = false;
		} else if (CommonUtils.isNullOrTrimedEmpty(properties.get(CommonConstant.DATASOURCE_USERNAME))) {
			valid = false;
			CommonUtils.log(MessageConstant.DATASOURCE_USERNAME_IS_EMPTY);
		}

		if (!properties.containsKey(CommonConstant.PRINT_ERROR_TRACE)) {
			CommonUtils.log(MessageConstant.PRINT_ERROR_TRACE_PROP_NOT_FOUND);
		} else if (CommonUtils.isNullOrTrimedEmpty(properties.get(CommonConstant.PRINT_ERROR_TRACE))) {
			CommonUtils.log(MessageConstant.PRINT_ERROR_TRACE_VALUE_EMPTY);
		} else {
			printStackTrace = properties.get(CommonConstant.PRINT_ERROR_TRACE).toString().toLowerCase().equals("true");
		}

		return valid;

	}

	private static String generateFileName() {
		return CommonUtils.pathTrailCheck(properties.get(CommonConstant.CSV_DEST_DIR).toString()) + CommonConstant.FILENAME_PREFIX + CommonUtils.getCurrentDateString()
				+ CommonConstant.DOT_CSV;
	}

	private static boolean isPropertiesFileExist() {
		try {

			properties = new Properties();

			InputStream stream;
			File f = CommonUtils.getFile(JAR_DIR + CommonConstant.PROPERTIES_FILE_DIR);
			stream = new FileInputStream(f);

			properties.load(stream);

			return true;

		} catch (Exception e) {

			CommonUtils.log(MessageConstant.PROPERTY_FILE_NOT_FOUND);
			printStackTrace(e);

			return false;

		}
	}

	private static void printStackTrace(Exception e) {
		if (printStackTrace) {
			CommonUtils.log(e.getMessage());
		}
	}

	// private static boolean isFinishedPrintCsv(List<ImportCustomerInfo>
	// customerInfoList) {
	//
	// boolean finished = false;
	//
	// Writer writer = null;
	// CSVWriter csvWriter = null;
	//
	// try {
	//
	// writer = Files.newBufferedWriter(Paths.get(generateFileName()));
	//
	// csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR,
	// CSVWriter.DEFAULT_QUOTE_CHARACTER,
	// CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
	//
	// csvWriter.writeNext(getDefaultHeadingList().toArray(new String[0]));
	//
	// for (ImportCustomerInfo cusInfo : customerInfoList) {
	//
	// csvWriter.writeNext(getDataAsList(cusInfo).toArray(new String[0]));
	// }
	//
	// csvWriter.flush();
	// csvWriter.close();
	// finished = true;
	//
	// } catch (IOException e) {
	//
	// CommonUtils.log(MessageConstant.ERROR_CSV_FILE_CREATION);
	//
	// printStackTrace(e);
	//
	// }
	//
	// return finished;
	// }

	private static ArrayList<String> getDefaultHeadingList() {
		ArrayList<String> headingList = new ArrayList<>();

		headingList.add(CommonConstant.CUSTOMER_NO);
		headingList.add(CommonConstant.CUSTOMER_NAME);
		headingList.add(CommonConstant.GENDER);
		headingList.add(CommonConstant.PHONE_NO);
		headingList.add(CommonConstant.NRC);
		headingList.add(CommonConstant.DOB);
		headingList.add(CommonConstant.SALARY);
		headingList.add(CommonConstant.DEL_FLAG);
		headingList.add(CommonConstant.COMPANY_NAME);
		headingList.add(CommonConstant.TOWNSHIP_ADDRESS);
		headingList.add(CommonConstant.MEMBERCARD_ID);

		if (maxAgreementCount == 0) {
			maxAgreementCount = 1;
		}

		for (int i = 1; i <= maxAgreementCount; i++) {
			headingList.add(CommonConstant.AGREEMENT_NO + i);
			headingList.add(CommonConstant.QR_SHOW + i);
			headingList.add(CommonConstant.FINANCIAL_AMT + i);
			headingList.add(CommonConstant.FINANCIAL_TERM + i);
			headingList.add(CommonConstant.FINANCIAL_STATUS + i);
		}

		return headingList;
	}

	private static ArrayList<String> getDataAsList(ImportCustomerInfo cusInfo) {
		ArrayList<String> dataList = new ArrayList<>();

		dataList.add(cusInfo.getCustomerNo());
		dataList.add(cusInfo.getName());
		dataList.add(cusInfo.getGender());
		dataList.add(cusInfo.getPhoneNo());
		dataList.add(cusInfo.getNrcNo());
		dataList.add(cusInfo.getDob());
		dataList.add(cusInfo.getSalary());
		dataList.add(cusInfo.getDelFlag());
		dataList.add(cusInfo.getCompanyName());
		dataList.add(cusInfo.getTownship());
		dataList.add(cusInfo.getMemberCardId());

		for (CustAgreementList agreementList : cusInfo.getCustAgreementListList()) {
			dataList.add(agreementList.getAgreementNo());
			dataList.add(agreementList.getQrShow());
			dataList.add(agreementList.getFinanceAmount());
			dataList.add(agreementList.getFinanceTerm());
			dataList.add(agreementList.getFinancialStatus());
		}

		return dataList;
	}

	private static void checkLastExecutedInfo() {

		File file = CommonUtils.getFile(CommonUtils.pathTrailCheck(JAR_DIR) + CommonConstant.DATA_EXPORT_HIDDEN_CONFIG_FILE_NAME);
		if (file != null) {
			try {

				Properties props = new Properties();
				props.load(new FileInputStream(file));
				if (props.containsKey(CommonConstant.LAST_EXECUTE_DATETIME) && !CommonUtils.isNullOrTrimedEmpty(props.get(CommonConstant.LAST_EXECUTE_DATETIME))) {

					lastExecutedTime_yyyymmddhhmmss_string = (String) props.get(CommonConstant.LAST_EXECUTE_DATETIME);

					if (!CommonUtils.isValidExecutionDateTimeString(lastExecutedTime_yyyymmddhhmmss_string)) {
						lastExecutedTime_yyyymmddhhmmss_string = "";
						CommonUtils.log(MessageConstant.INVALID_EXECUTION_DATETIME_FORMAT);
					} else {
						if ("" != lastExecutedTime_yyyymmddhhmmss_string) {

							String date = lastExecutedTime_yyyymmddhhmmss_string.substring(0, 8);
							String time = lastExecutedTime_yyyymmddhhmmss_string.substring(8);

							// WHERE_CLAUSE = " WHERE ( c.cretime >= '" + time +
							// "' OR c.updtime >= '" + time + "' ) AND ("
							// + " c.credate >= '" + date + "' OR c.upddate >=
							// '" + date + "' ) ";

							WHERE_CLAUSE = " AND ( c.cretime >= '" + time + "' AND c.credate >= '" + date + "' ) OR (" + " c.updtime >= '" + time + "' AND c.upddate >= '" + date
									+ "' ) ";

						}
					}
				}

			} catch (IOException e) {
				CommonUtils.log(MessageConstant.ERROR_FROM_CHECK_LAST_EXECUTED_INFO);
				printStackTrace(e);
			}
		}
	}

	private static void saveLastExecutedInfo() {

		String hiddenFileDir = CommonUtils.pathTrailCheck(JAR_DIR) + CommonConstant.DATA_EXPORT_HIDDEN_CONFIG_FILE_NAME;

		File file = CommonUtils.getFile(hiddenFileDir);
		FileOutputStream fos = null;

		try {
			if (file == null) {
				file = new File(hiddenFileDir);
			}

			fos = new FileOutputStream(file, false);

			Properties props = new Properties();

			props.put(CommonConstant.LAST_EXECUTE_DATETIME, executionStartTime_yyyymmddhhmmss_string);
			props.store(fos, "DATA_FORMAT_YYYYMMDDHHMMSS");

			fos.close();
			Files.setAttribute(file.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
			file.setWritable(true);
			file.setWritable(true);

		} catch (FileNotFoundException e) {
			CommonUtils.log(MessageConstant.ERROR_FROM_SAVE_LAST_EXECUTED_INFO);
			printStackTrace(e);
		} catch (IOException ioe) {
			CommonUtils.log(MessageConstant.ERROR_FROM_SAVE_LAST_EXECUTED_INFO);
			printStackTrace(ioe);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException ioe) {
				CommonUtils.log(MessageConstant.ERROR_FROM_SAVE_LAST_EXECUTED_INFO);
				printStackTrace(ioe);
			}

		}

	}

}