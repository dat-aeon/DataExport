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
import mm.com.aeon.vcs.data.export.model.T_paymentList;

public class DataExportApplicationForLastPaymentDate {

	private static boolean printStackTrace;

	public static String JAR_DIR = "/";

	private static Properties properties;

	private static int totalRecords = 0;

	private static String lastExecutedTime_yyyymmddhhmmss_string = "";

	private static String executionStartTime_yyyymmddhhmmss_string = "";

	private static String WHERE_CLAUSE = "";

	private static int recordsPerEachFile;

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

		List<String> tPaymentList = new ArrayList<>();
		Connection connection = null;

		if (!isPropertiesFileExist()) {
			CommonUtils.log(MessageConstant.PROCESS_END);
			return;
		}

		if (!isValidProperties()) {
			CommonUtils.log(MessageConstant.PROCESS_END);
			return;
		}
		recordsPerEachFile = Integer.parseInt(properties.getProperty(CommonConstant.RECORDS_PER_EACH_FILE));

		connection = getConnection();

		if (null == connection) {
			CommonUtils.log(MessageConstant.PROCESS_END);
			return;
		}

		checkLastExecutedInfo();

		int rowCount = 0;
		Writer writer = null;
		CSVWriter csvWriter = null;

		Statement statement = null;
		ResultSet rs = null;

		try {

			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = null;

			CommonUtils.log("Start payment count query");
			// Get total data count
			rs = statement.executeQuery(getCustomerCountQueryString());
			if (rs.next()) {
				totalRecords = rs.getInt(CommonConstant.RECORD_COUNT);
			}
			CommonUtils.log("End payment count query");

			CommonUtils.log("[Total Records : " + totalRecords + "]");

			if (totalRecords < 1) {
				connection.close();
				CommonUtils.log(MessageConstant.NO_DATA_TO_EXPORT);
				CommonUtils.log(MessageConstant.PROCESS_FINISH);
				return;
			}

			// check max agreement count

			rs = statement.executeQuery(getMainQueryString());
			CommonUtils.log("End customer data query");

			while (rs.next()) {

				if (!tPaymentList.contains(rs.getString(CommonConstant.AGREEMENT_CD).trim())) {

					T_paymentList t_payment = new T_paymentList();

					if (null == rs.getString(CommonConstant.AGREEMENT_CD)) {
						CommonUtils.invalidProperty(CommonConstant.AGREEMENT_CD, "");
						continue;
					}
					t_payment.setAgreementCd(rs.getString(CommonConstant.AGREEMENT_CD).trim());

					if (null == rs.getString(CommonConstant.PAYMENT_DATE)) {
						CommonUtils.invalidProperty(CommonConstant.PAYMENT_DATE, t_payment.getAgreementCd());
						continue;
					}
					t_payment.setPaymentDate(rs.getString(CommonConstant.PAYMENT_DATE).trim());

					// customerInfoList.add(customerInfo);
					tPaymentList.add(rs.getString(CommonConstant.AGREEMENT_CD).trim());

					if (rowCount % recordsPerEachFile == 0) {

						if (null != csvWriter) {
							csvWriter.flush();
							csvWriter.close();
							csvWriter = null;
						}

						if (null == csvWriter) {
							writer = Files.newBufferedWriter(Paths.get(generateFileName()));
						}

						csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
								CSVWriter.RFC4180_LINE_END);
						csvWriter.writeNext(getDefaultHeadingList().toArray(new String[0]));

					}

					if (t_payment != null) {
						csvWriter.writeNext(getDataAsList(t_payment).toArray(new String[0]));
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

	private static String getCustomerCountQueryString() {
		return "select sum(agreementcount) as recordcount from (select count(distinct(p.agreementcd)) as agreementcount from t_payment p where p.receipttype in (1,4,8) and p.delflag = 0 "
				+ WHERE_CLAUSE + " group by p.agreementcd) as totalcount";
	}

	private static String getMainQueryString() {
		return "select " + "p.agreementcd as " + CommonConstant.AGREEMENT_CD + ", max(p.paymentdate) AS " + CommonConstant.PAYMENT_DATE
				+ " from t_payment p where p.receipttype in (1,4,8) and p.delflag = 0 " + WHERE_CLAUSE + " group by p.agreementcd order by p.agreementcd desc";
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

		headingList.add(CommonConstant.AGREEMENT_CD);
		headingList.add(CommonConstant.PAYMENT_DATE);

		return headingList;
	}

	private static ArrayList<String> getDataAsList(T_paymentList t_payment) {
		ArrayList<String> dataList = new ArrayList<>();

		dataList.add(t_payment.getAgreementCd());
		dataList.add(t_payment.getPaymentDate());

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

							WHERE_CLAUSE = " AND ( CONCAT (p.credate, p.cretime) >= '" + date + time + "' ) OR (" + " CONCAT (p.upddate, p.updtime) >= '" + date + time + "' )";

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
		if (file != null) {
			file.delete();
		} else {
			file = new File(hiddenFileDir);
		}

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file, false);

			Properties props = new Properties();

			props.put(CommonConstant.LAST_EXECUTE_DATETIME, executionStartTime_yyyymmddhhmmss_string);
			props.store(fos, "DATA_FORMAT_YYYYMMDDHHMMSS");

			fos.close();
			Files.setAttribute(file.toPath(), "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
			file.setWritable(true);
			file.setReadable(true);

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