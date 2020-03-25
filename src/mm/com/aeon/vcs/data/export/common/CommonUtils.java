package mm.com.aeon.vcs.data.export.common;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

	public static String getLogFileName(String jarDir) {
		String folder = pathTrailCheck(jarDir + CommonConstant.LOG_FOLDER);
		dirNotExists(folder, true);
		return folder + CommonConstant.FILENAME_PREFIX + getCurrentDateString() + CommonConstant.DOT_LOG;
	}

	public static String getCurrentDateString() {
		DateFormat dateFormat = new SimpleDateFormat(CommonConstant.FILE_NAME_DATE_FORMAT);
		return dateFormat.format(getCurrentDateTime());
	}

	public static String getExectutionStartTimeString() {
		DateFormat dateFormat = new SimpleDateFormat(CommonConstant.EXECUTION_DATETIME_FORMAT);
		return dateFormat.format(getCurrentDateTime());
	}

	public static String getCurrentTimeString() {
		DateFormat dateFormat = new SimpleDateFormat(CommonConstant.TIME_FORMAT);
		return dateFormat.format(getCurrentDateTime());
	}

	public static Date getCurrentDateTime() {
		return Calendar.getInstance().getTime();
	}

	public static void log(String str) {
		System.out.println(getCurrentTimeString() + "\t" + str);
	}

	public static File[] getCSVFileInDir(String dir) throws IOException {
		File[] csvFiles = new File(dir.trim()).listFiles((file) -> {
			return file.getName().endsWith(".csv");
		});
		return csvFiles;
	}

	public static boolean isNullOrTrimedEmpty(Object object) {
		if (null == object) {
			return true;
		} else if (object.toString().trim().isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean isValidExecutionDateTimeString(String str) {
		return isStringMatchPattern(str, CommonConstant.EXECUTION_DATETIME_PATTERN);
	}

	public static boolean isStringMatchPattern(String text, String patternString) {
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(text);
		return matcher.matches();
	}

	public static void invalidProperty(String field, String customerNo) {
		CommonUtils.log("[" + field + "] of Customer No - " + customerNo + " is invalid value.");
	}

	public static boolean dirNotExists(String dir, boolean createIfNot) {

		File file = new File(dir);

		if (createIfNot) {
			if (!file.exists()) {
				file.mkdir();
			}
		}

		return !file.exists();
	}

	public static String pathTrailCheck(String path) {
		if (path.charAt(path.length() - 1) != File.separatorChar) {
			path += File.separator;
		}
		return path;
	}

	public static File getFile(String path) {
		File file = null;

		try {

			file = new File(path);

			if (!file.exists()) {
				file = null;
			}

			if (!file.isFile()) {
				file = null;
			}

		} catch (Exception e) {
			file = null;
		}

		return file;
	}

}
