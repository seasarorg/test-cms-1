/*
 * Copyright 2004-2007 the Seasar Foundation and the Others..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
