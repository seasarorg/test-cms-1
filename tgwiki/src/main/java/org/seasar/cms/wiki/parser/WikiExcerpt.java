/* Generated By:JJTree: Do not edit this line. WikiExcerpt.java */

package org.seasar.cms.wiki.parser;

public class WikiExcerpt extends SimpleNode {
  public WikiExcerpt(int id) {
    super(id);
  }

  public WikiExcerpt(WikiParser p, int id) {
    super(p, id);
  }

  public int level;
  
  /** Accept the visitor. **/
  public Object jjtAccept(WikiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
