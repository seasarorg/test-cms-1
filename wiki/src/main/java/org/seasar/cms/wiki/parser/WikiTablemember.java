/* Generated By:JJTree: Do not edit this line. WikiTablemember.java */

package org.seasar.cms.wiki.parser;

public class WikiTablemember extends SimpleNode {
  public WikiTablemember(int id) {
    super(id);
  }

  public WikiTablemember(WikiParser p, int id) {
    super(p, id);
  }

  public int type;  

  /** Accept the visitor. **/
  public Object jjtAccept(WikiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}