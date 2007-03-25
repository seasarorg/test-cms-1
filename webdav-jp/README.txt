■webdav-jpとは

webdav-jpはServletフィルタを用いてTomcat4.Xに付属しているWebDAV Webア
プリケーションで日本語ファイル名のファイルを扱うことができるようにした
Webアプリケーションです。


■動作環境

webdav-jpを利用できる環境は以下の通りです。
・Jakarta Tomcat 4.X

ServletコンテナとしてはTomcat4.1.24で動作を確認していますが、ServletAPI
2.3準拠のServletコンテナであれば多分動作すると思います。またJDKは
Sun JDK1.4で動作確認しています。

また、動作を確認しているWebDAVクライアントは以下の通りです。
・Windows98 Seconde EditionのWebフォルダ
・Windows2000 ProfessionalのWebフォルダ
・Windows用のWebDrive（http://www.southrivertech.com/）


■使用方法

アーカイブを展開してできるファイルwebdav-jp.warをTomcatのWebアプリケー
ションとして配備します。webdav-jp.warに含まれるWEB-INF/web.xmlは適宜
修正して下さい。

web.xml中の<servlet>タグに書くことのできる初期パラメータ（init-param）
としては次のようなものがあります。
・listings
    ディレクトリにアクセスした際にディレクトリの内容を一覧表示するか
    どうか。初期値はtrueです。
・readonly
    読み込み専用にするかどうか。ファイルをアップロードしたり修正した
    りしたい場合はfalseにする必要があります。初期値はfalseです。

web.xml中の<filter>タグに書くことのできる初期パラメータ（init-param）
としては次のようなものがあります。
・nativeEncoding
    リクエストURLの文字エンコーディング。URLは本来マルチバイト文字を
    含んではいけませんが、クライアントによってはマルチバイト文字を含
    むURLをリクエストすることがあります。このパラメータを適切に指定
    することでこのような行儀の悪いクライアントからのリクエストを正し
    く解釈できるようになります。
・nativeEncoding.XXXX
    リクエストのUser-Agentヘッダの値がXXXXという文字列を含むようなク
    ライアントに関するnativeEncoding指定です。マッチするnativeEncoding.XXXX
    指定が複数ある場合はマッチする部分が長いものが優先されます。また、
    マッチするnativeEncoding.XXXX指定がない場合はnativeEncodingの値が
    使用されます。
・urlEncoding
    リクエストのURLのURLエンコーディングの文字エンコーディング。URL
    エンコーディングとは、マルチバイト文字を「%文字コード」形式でエ
    ンコードしたものです。このURLエンコーディングの文字コードが何か
    を指定します。
・urlEncoding.XXXX
    リクエストのUser-Agentヘッダの値がXXXXという文字列を含むようなク
    ライアントに関するurlEncoding指定です。マッチするurlEncoding.XXXX
    指定が複数ある場合はマッチする部分が長いものが優先されます。また、
    マッチするurlEncoding.XXXX指定がない場合はurlEncodingの値が使用さ
    れます。

通常はデフォルトの設定で問題ないと思いますが、アクセス制限をかけたい場
合はweb.xml中の<security-role>指定を変更して下さい。


■ライセンス

このプログラムはApache Software License Version 2.0に従って配布されます。


■その他

このプログラムに関する質問やお問い合わせ、バグ報告はskirnir@t3.rim.or.jp
までお願いします。


■変更履歴

0.1.6:

0.1.5:
・URLに「;jsessionid=XXX」のような、セミコロンで区切られたパラメータが渡された
  場合に、補正後のservletPathやpathInfoにパラメータがついてしまう不具合を修正した。
・リクエストがフォワードされた後にservletPathやpathInfoなどを取得するとフォワード以前の
  情報が返されてしまう不具合を修正した。
・Webアプリケーション内のパスにリダイレクトをする際に、パスが日本語を含む場合は%XX形式に
  エンコードするようにした。
0.1.4: 2004/04/16
・useServletPathAsPathInfoを指定した時に、今まではservletPathとpathInfoが
  ともに非nullの場合は何もしなかったのを止めて、servletPathとpathInfoを連
  結した文字列をpathInfoとして設定し、servletPathは空文字列として設定する
  ようにした。これは、servlet-mappingとしてprefix matchingを指定した場合
  に正しくパスを解釈できないケースがあったため。
・urlDecode.destinationパラメータの名称をdestination.urlDecodeに変更した。
・Tomcat4.1.29からTomcat4.1.30に付属のWebdavServletにはDestinationヘッダ
  の解釈を正常に行なえない不具合があり、これを回避するために
  destination.absolutePathパラメータを追加した。
・'+'を含んでいるURLに正常にアクセスできない不具合を解消するため、URL中の
  '+'を空白に解釈しないようにした。
・path infoがnullである場合に強制的に空文字列に置き換えるためのパラメータ
  disallowNullPathInfoを追加した。これは例えばTomcatのWebdavServletで/DAV/*
  のようなマッピングをする際に必要になる。
0.1.3: 2003/09/30
・nativeEncoding、urlEncodingのマッチング順序が不定だったので、User-Agent
  パターンが長いものから順にマッチングするように変更した。
・WindowsXPのWebフォルダに対応した。
・ResinのWebDavServletでも使えるように修正した。
・WebDriveについては、「Always send URLs as UTF-8」チェックをしていないと
  動かないようにした。これは、MS932でURLが送られるとResinではリクエストが
  Servletに届く前に拒絶されてしまうため。
・ResinのWebDavServletをKvasir/Soraで使用する時に生じる問題点を解消するた
  めのオプションuseServletPathAsPathInfoを追加した。
・説明文を最新のものに合わせて修正した。

0.1.2: 2002/09/19
・MOVE等の、Destinationヘッダを持つリクエストに対応できていなかった不具合
  を修正。具体的にはDestinationヘッダの内容も文字化けを修正するようにした。
・web.xml中で、nativeEncoding.PATTERNのようにパラメータ指定をしておくこと
  で、UserAgentの文字列にPATTERNが部分一致したパラメータを採用するように
  した。urlEncoding.PATTERNに関しても同様。
0.1.1: 2002/09/06
・Tomcat4はリクエストURI中の \（0x5c）を /（0x2f）に変換してしまうので、
  WindowsのWebフォルダのようにシフトJISをベタでリクエストURIに含める場合、
  例えば「表」（0x95 0x5c）のように2バイト目に0x5cを含む文字が化けてしま
  う。この問題に対処した。

0.1.0: 2002/09/05
・最初のバージョン。


以上
