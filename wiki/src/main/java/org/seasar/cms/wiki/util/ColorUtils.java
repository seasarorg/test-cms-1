package org.seasar.cms.wiki.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ColorUtils {

	private static Log log_ = LogFactory.getLog(ColorUtils.class);

	private static Map<String, Color> COLOR_MAP = new HashMap<String, Color>();

	static {
		COLOR_MAP.put("red", Color.RED);
		COLOR_MAP.put("blue", Color.BLUE);
		COLOR_MAP.put("black", Color.BLACK);
		COLOR_MAP.put("cyan", Color.CYAN);
		COLOR_MAP.put("green", Color.GREEN);
		COLOR_MAP.put("gray", Color.GRAY);
	}

	public static Color getColorByString(String s, boolean foreground) {
		Color c = (foreground) ? Color.BLACK : Color.WHITE; // default
		if (s.startsWith("#")) {
			String code = "0x" + s.substring(1);
			try {
				c = Color.decode(Integer.decode(code).toString());
			} catch (Exception e) {
				log_.error(e.getMessage());
			}
		} else {
			// for color name like "blue", "red"
			if (COLOR_MAP.get(s.toLowerCase()) != null) {
				c = COLOR_MAP.get(s.toLowerCase());
			}
		}
		return c;
	}
}
