/* Generated By:JJTree: Do not edit this line. WikiSyntaxError.java */

package org.seasar.cms.wiki.parser;

public class WikiSyntaxError extends SimpleNode {
  public WikiSyntaxError(int id) {
    super(id);
  }

  public WikiSyntaxError(WikiParser p, int id) {
    super(p, id);
  }
  
  public String letter;


  /** Accept the visitor. **/
  public Object jjtAccept(WikiParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}