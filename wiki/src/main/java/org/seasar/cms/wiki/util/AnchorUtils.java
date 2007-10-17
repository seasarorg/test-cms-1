package org.seasar.cms.wiki.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nishioka
 */
public class AnchorUtils {

	private static final String PATTERN = ".*\\[#[a-zA-Z][0-9a-zA-Z]+\\]$";

	private static final Pattern NEWLINE = Pattern.compile("\n");

	private static final Pattern ANCHOR = Pattern.compile(PATTERN);

	public static final String PREFIX = "[#tg";

	public static final String SUFFIX = "]";

	/**
	 * generate anchor data from object hashcode
	 * 
	 * @param contents
	 *            wiki contents
	 * @return contents with anchor data
	 */
	public static String setToHeading(String contents) {
		String mod = WikiStringUtils.removeCarriageReturn(contents);
		String[] lines = NEWLINE.split(mod);

		StringBuffer buf = new StringBuffer();
		int hashcode = contents.hashCode();
		for (String line : lines) {
			buf.append(line);
			Matcher m = ANCHOR.matcher(line);
			if (line.indexOf("*") == 0 && !m.matches()) {
				buf.append(PREFIX + Integer.toHexString(hashcode) + SUFFIX);
				hashcode++;
			}
			buf.append("\n");
		}
		return buf.toString();
	}
}
