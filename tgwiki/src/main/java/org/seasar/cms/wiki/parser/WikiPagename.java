/* Generated By:JJTree: Do not edit this line. WikiPagename.java */

package org.seasar.cms.wiki.parser;

public class WikiPagename extends SimpleNode {
  public WikiPagename(int id) {
    super(id);
  }

  public WikiPagename(WikiParser p, int id) {
    super(p, id);
  }
  
  public String image; 

  /** Accept the visitor. **/
  public Object jjtAccept(WikiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}