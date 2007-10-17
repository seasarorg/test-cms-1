package org.seasar.cms.wiki.util;

import javax.imageio.ImageIO;

public class ImageUtils {

	private static final String[] IMAGE_SUFFIX = ImageIO.getReaderFormatNames();

	/**
	 * Discriminates the specifid string represents image file or not. This see
	 * the string as image if it ends with <strong>.jpg,.jpeg,.gif,.png,and .bmp</strong>
	 * in a case insensitive mannger.
	 */
	public static boolean isImage(String s) {
		if (s == null) {
			return false;
		}
		for (String extension : IMAGE_SUFFIX) {
			if (s.toLowerCase().endsWith("." + extension)) {
				return true;
			}
		}
		return false;
	}
}
