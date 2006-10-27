WebDAVサーブレット
------------------

このソフトウェアは、Tomcat-5.5が持つWebDAVサーブレットを単にTomcatから独立させたものです。


使い方
======

webdav-VERNO.jarをWEB-INF/lib/の下に置き、web.xmlの中で例えば以下のような記述を
して下さい::

  <servlet>
    <servlet-name>webdav</servlet-name>
    <servlet-class>org.apache.catalina.servlets.WebdavServlet</servlet-class>
    <init-param>
      <param-name>debug</param-name>
      <param-value>0</param-value>
    </init-param>
    <init-param>
      <param-name>listings</param-name>
      <param-value>true</param-value>
    </init-param>
    <init-param>
      <param-name>readonly</param-name>
      <param-value>false</param-value>
    </init-param>
  </servlet>

  <servlet-mapping>
    <servlet-name>webdav</servlet-name>
    <url-pattern>/*</url-pattern>
  </servlet-mapping>

このWebDAVサーブレットの操作対象であるリソースはServletContextに
「org.apache.catalina.resources」というキーでsetAttributeされている
org.apache.naming.resources.ProxyDirContextオブジェクトです。従って、
事前に（WebDAVサーブレットの起動よりも前に）このオブジェクトをServletContext
にバインドしておく必要があります。

なお、Tomcat-5.5.xであれば、コンテナがファイルシステム操作用のProxyDirContext
オブジェクトを予めバインドしてくれるため、ProxyDirContextを用意しなくてもとりあえず
上記の設定だけで動きます。（操作対象はWebアプリケーションの持つリソースになります。）


更新履歴
========

5.5.20
  Tomcat-5.5.20のソースコードから抽出した。


以上
