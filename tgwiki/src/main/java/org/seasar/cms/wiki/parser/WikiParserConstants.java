/* Generated By:JJTree&JavaCC: Do not edit this line. WikiParserConstants.java */
package org.seasar.cms.wiki.parser;

public interface WikiParserConstants {

  int EOF = 0;
  int ALPHABET = 3;
  int HEX = 4;
  int DIGIT = 5;
  int NOASCII = 6;
  int HOSTNAME = 7;
  int PLUGINNAME = 8;
  int NL = 9;
  int TILDE = 10;
  int SEMICOLON = 11;
  int LPAREN = 12;
  int RPAREN = 13;
  int LBRACKET = 14;
  int RBRACKET = 15;
  int LBRACE = 16;
  int RBRACE = 17;
  int SLASH = 18;
  int LT = 19;
  int EXCERPT1 = 20;
  int EXCERPT2 = 21;
  int EXCERPT3 = 22;
  int LIST1 = 23;
  int LIST2 = 24;
  int LIST3 = 25;
  int NLIST1 = 26;
  int NLIST2 = 27;
  int NLIST3 = 28;
  int PRESHAPED = 29;
  int HEADING1 = 30;
  int HEADING2 = 31;
  int HEADING3 = 32;
  int ALIGN1 = 33;
  int ALIGN2 = 34;
  int ALIGN3 = 35;
  int FALIGN1 = 36;
  int FALIGN2 = 37;
  int HORIZONTAL = 38;
  int BLOCKPLUGIN = 39;
  int COLORWORD = 40;
  int BGCOLORWORD = 41;
  int SIZEWORD = 42;
  int COLSPAN = 43;
  int TABLE = 44;
  int DLIST = 45;
  int CTABLE = 46;
  int PIPE = 47;
  int COLON = 48;
  int COMMA = 49;
  int TABLEDEL = 50;
  int NTABCOLSPAN = 51;
  int NTABROWSPAN = 52;
  int CTABLEDEL = 53;
  int ANCHORNAME = 54;
  int URL = 55;
  int EMAIL = 56;
  int NAME = 57;
  int PLUGINARG = 58;
  int DELETELINE = 59;
  int STRONGITALIC = 60;
  int WIKINAME = 61;
  int NATIVELINK = 62;
  int ANCHOR = 63;
  int PAGENAME = 64;
  int LINK = 65;
  int INTERWIKI = 66;
  int ALIASLINK = 67;
  int ALIAS = 68;
  int IDENTIFIER = 69;
  int ARGS = 70;
  int INLINEPLUGIN = 71;
  int ANYOTHER = 72;

  int CTAB = 0;
  int NTAB = 1;
  int DEFAULT = 2;

  String[] tokenImage = {
    "<EOF>",
    "\"\\r\"",
    "<token of kind 2>",
    "<ALPHABET>",
    "<HEX>",
    "<DIGIT>",
    "<NOASCII>",
    "<HOSTNAME>",
    "<PLUGINNAME>",
    "\"\\n\"",
    "\"~\"",
    "\";\"",
    "\"(\"",
    "\")\"",
    "\"[\"",
    "\"]\"",
    "\"{\"",
    "\"}\"",
    "\"/\"",
    "\"<\"",
    "\">\"",
    "\">>\"",
    "\">>>\"",
    "\"-\"",
    "\"--\"",
    "\"---\"",
    "\"+\"",
    "\"++\"",
    "\"+++\"",
    "\" \"",
    "\"*\"",
    "\"**\"",
    "\"***\"",
    "\"LEFT:\"",
    "\"CENTER:\"",
    "\"RIGHT:\"",
    "<FALIGN1>",
    "<FALIGN2>",
    "\"----\"",
    "<BLOCKPLUGIN>",
    "<COLORWORD>",
    "<BGCOLORWORD>",
    "<SIZEWORD>",
    "\"==\"",
    "\"\\n|\"",
    "\"\\n:\"",
    "\"\\n,\"",
    "\"|\"",
    "\":\"",
    "\",\"",
    "\"|\"",
    "\">|\"",
    "\"~|\"",
    "\",\"",
    "<ANCHORNAME>",
    "<URL>",
    "<EMAIL>",
    "<NAME>",
    "<PLUGINARG>",
    "\"%%\"",
    "<STRONGITALIC>",
    "<WIKINAME>",
    "<NATIVELINK>",
    "<ANCHOR>",
    "<PAGENAME>",
    "<LINK>",
    "<INTERWIKI>",
    "<ALIASLINK>",
    "<ALIAS>",
    "<IDENTIFIER>",
    "<ARGS>",
    "<INLINEPLUGIN>",
    "<ANYOTHER>",
    "\"((\"",
    "\"))\"",
  };

}