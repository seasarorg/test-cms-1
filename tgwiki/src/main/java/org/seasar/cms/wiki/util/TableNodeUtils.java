package org.seasar.cms.wiki.util;

import java.util.HashMap;
import java.util.Map;

import org.seasar.cms.wiki.parser.SimpleNode;
import org.seasar.cms.wiki.parser.WikiCSVTable;
import org.seasar.cms.wiki.parser.WikiTable;
import org.seasar.cms.wiki.parser.WikiTablecolumn;
import org.seasar.cms.wiki.parser.WikiTablemember;


/**
 * @author nishioka
 */
public class TableNodeUtils {

	public static Map<String, String> getTdAttributes(WikiTablecolumn node) {
		Map<String, String> attrs = new HashMap<String, String>();
		String style = TableNodeUtils.getTdStyle(node);
		if (style.length() > 0) {
			attrs.put("style", style);
		}
		if (node.colspannum > 0) {
			attrs.put("colspan", ++node.colspannum + "");
		}
		if (node.rowspannum > 0) {
			attrs.put("rowspan", ++node.rowspannum + "");
		}
		return attrs;
	}

	/**
	 * Before accepting node tree, need to set node properties for colspan,
	 * rowspan.
	 * 
	 * The value of colspan or rowspan which might be set after trailing should
	 * be incremented before being used in visitor.
	 */
	public static void prepareWikiTable(SimpleNode node, Object data) {
	
		if (!(node instanceof WikiTable) && !(node instanceof WikiCSVTable))
			throw new IllegalArgumentException(
					"The class other than WikiTable or WikiCSVTable cannot be accepted.");
	
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			if (!(node.jjtGetChild(i) instanceof WikiTablemember)) {
				continue;
			}
			WikiTablemember member = (WikiTablemember) node.jjtGetChild(i);
			int colspannum = 0;
			for (int j = 0; j < member.jjtGetNumChildren(); j++) {
				WikiTablecolumn column = (WikiTablecolumn) member
						.jjtGetChild(j);
				if (column.iscolspan) {
					colspannum++;
				} else if (column.isrowspan) {
					int rowspannum = 1;
					for (int k = i - 1; k >= 0; k--) {
						// Access to previous WikiTablemember object to set
						// rowspan
						if (node.jjtGetChild(k) instanceof WikiTablemember) {
							WikiTablecolumn leftcolumn = (WikiTablecolumn) node
									.jjtGetChild(k).jjtGetChild(j);
							if (leftcolumn.isrowspan) {
								rowspannum++;
								continue;
							} else {
								leftcolumn.rowspannum = rowspannum;
								break;
							}
						} else { // in case WikiError
							break;
						}
					}
				} else {
					if (colspannum > 0) {
						column.colspannum = colspannum;
						colspannum = 0;
					}
				}
			}
		}
	}
	
	
	// ----- [private method] -----
	
	
	private static String getTdStyle(WikiTablecolumn node) {
		StringBuilder style = new StringBuilder();
		if (node.align != null) {
			style.append("text-align:" + node.align + ";");
		}
		if (node.bgcolor != null) {
			style.append("background-color:" + node.bgcolor + ";");
		}
		if (node.color != null) {
			style.append("color:" + node.color + ";");
		}
		if (node.size != null) {
			style.append("font-size:" + node.size + ";");
		}
		return style.toString();
	}

	
}
