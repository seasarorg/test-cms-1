package org.seasar.cms.mailsender.impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ozacc.mail.Mail;
import com.ozacc.mail.MailAuthenticationException;
import com.ozacc.mail.MailBuildException;
import com.ozacc.mail.MailException;
import com.ozacc.mail.MailSendException;
import com.ozacc.mail.SendMail;
import com.ozacc.mail.impl.MimeMessageBuilder;
import com.ozacc.mail.impl.OMLMimeMessage;

/**
 * SendMailインターフェースの実装クラス。
 *
 * @see {@link com.ozacc.mail.impl.SendMailImpl}
 */
public class SendMailImpl implements SendMail {
    private static Log log = LogFactory.getLog(SendMailImpl.class);

    /** デフォルトのプロトコル。「smtp」 */
    public static final String DEFAULT_PROTOCOL = "smtp";

    /**
     * デフォルトのポート。「-1」<br>
     * -1はプロトコルに応じた適切なポートを設定する特別な値。
     * */
    public static final int DEFAULT_PORT = -1;

    /** デフォルトのSMTPサーバ。「localhost」 */
    public static final String DEFAULT_HOST = "localhost";

    /** ISO-2022-JP */
    public static final String JIS_CHARSET = "ISO-2022-JP";

    private static final String RETURN_PATH_KEY = "mail.smtp.from";

    /** 接続タイムアウト */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    /** 読込タイムアウト */
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    private String protocol = DEFAULT_PROTOCOL;

    private String host = DEFAULT_HOST;

    private int port = DEFAULT_PORT;

    private String username;

    private String password;

    private String charset = JIS_CHARSET;

    private String returnPath;

    private String messageId;

    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    private int readTimeout = DEFAULT_READ_TIMEOUT;

    private boolean starttlsEnabled;

    private Properties properties = new Properties();

    /**
     * コンストラクタ。
     */
    public SendMailImpl() {
    }

    /**
     * コンストラクタ。使用するSMTPサーバを指定します。
     * 
     * @param host SMTPサーバのホスト名、またはIPアドレス
     */
    public SendMailImpl(String host) {
        this();
        setHost(host);
    }

    /**
     * @see com.ozacc.mail.SendMail#send(com.ozacc.mail.Mail)
     */
    public void send(Mail mail) throws MailException {
        send(new Mail[] { mail });
    }

    /**
     * @see com.ozacc.mail.SendMail#send(com.ozacc.mail.Mail[])
     */
    public void send(Mail[] mails) throws MailException {
        MimeMessageWrapper[] mmws = new MimeMessageWrapper[mails.length];
        Session session = Session.getInstance(new Properties());
        for (int i = 0; i < mails.length; i++) {
            Mail mail = mails[i];

            // MimeMessageを生成
            MimeMessage message = createMimeMessage(session);
            if (isMessageIdCustomized()) {
                mail.addHeader("Message-ID", ((OMLMimeMessage) message)
                        .getMessageId());
            }
            MimeMessageBuilder builder = new MimeMessageBuilder(message,
                    charset);
            try {
                builder.buildMimeMessage(mail);
            } catch (UnsupportedEncodingException e) {
                throw new MailBuildException("サポートされていない文字コードが指定されました。", e);
            } catch (MessagingException e) {
                throw new MailBuildException("MimeMessageの生成に失敗しました。", e);
            }

            // Return-Pathを取得
            String returnPath;
            if (mail.getReturnPath() != null) {
                returnPath = mail.getReturnPath().getAddress();
            } else {
                returnPath = this.returnPath;
            }

            mmws[i] = new MimeMessageWrapper(message, returnPath, mail
                    .getEnvelopeTo());
        }
        processSend(mmws);
    }

    /**
     * @see com.ozacc.mail.SendMail#send(javax.mail.internet.MimeMessage)
     */
    public void send(MimeMessage message) throws MailException {
        send(new MimeMessage[] { message });
    }

    /**
     * @see com.ozacc.mail.SendMail#send(javax.mail.internet.MimeMessage[])
     */
    public void send(MimeMessage[] messages) throws MailException {
        MimeMessageWrapper[] mmws = new MimeMessageWrapper[messages.length];
        for (int i = 0; i < messages.length; i++) {
            mmws[i] = new MimeMessageWrapper(messages[i], returnPath);
        }
        processSend(mmws);
    }

    private void processSend(MimeMessageWrapper[] mmws) throws MailException {

        Properties prop = new Properties();
        // タイムアウトの設定
        prop.put("mail.smtp.connectiontimeout", String
                .valueOf(connectionTimeout));
        prop.put("mail.smtp.timeout", String.valueOf(readTimeout));
        //  mail.smtp.authプロパティの設定
        if (username != null && !"".equals(username) && password != null
                && !"".equals(password)) {
            prop.put("mail.smtp.auth", "true");
        }
        // STARTTLSの設定
        if (starttlsEnabled) {
            prop.put("mail.smtp.starttls.enable", String.valueOf(true));
        }
        // その他の設定
        for (Enumeration<?> enm = properties.propertyNames(); enm
                .hasMoreElements();) {
            String key = (String) enm.nextElement();
            prop.put(key, properties.getProperty(key));
        }
        Session session = Session.getInstance(prop);

        Transport transport = null;
        try {
            // SMTPサーバに接続
            log.debug("SMTPサーバ[" + host + "]に接続します。");
            transport = session.getTransport(protocol);
            transport.connect(host, port, username, password);
            log.debug("SMTPサーバ[" + host + "]に接続しました。");

            for (int i = 0; i < mmws.length; i++) {
                MimeMessage mimeMessage = mmws[i].getMimeMessage();
                //  Return-Pathをセット
                String returnPath = mmws[i].getReturnPath();
                if (returnPath != null) {
                    session.getProperties().put(RETURN_PATH_KEY, returnPath);
                    log.debug("Return-Path[" + returnPath + "]を設定しました。");
                }
                // 送信日時をセット
                mimeMessage.setSentDate(new Date());
                mimeMessage.saveChanges();

                // 送信
                log.debug("メールを送信します。");
                if (mmws[i].hasEnvelopeTo()) {
                    log.debug("メールはenvelope-toアドレスに送信されます。");
                    transport.sendMessage(mimeMessage, mmws[i].getEnvelopeTo());
                } else {
                    transport.sendMessage(mimeMessage, mimeMessage
                            .getAllRecipients());
                }
                log.debug("メールを送信しました。");

                // Return-Pathを解除
                if (returnPath != null) {
                    session.getProperties().remove(RETURN_PATH_KEY);
                    log.debug("Return-Path設定をクリアしました。");
                }
            }
        } catch (AuthenticationFailedException ex) {
            log.error("SMTPサーバ[" + host + "]への接続認証に失敗しました。", ex);
            throw new MailAuthenticationException(ex);
        } catch (MessagingException ex) {
            log.error("メールの送信に失敗しました。", ex);
            throw new MailSendException("メールの送信に失敗しました。", ex);
        } finally {
            if (transport != null && transport.isConnected()) {
                log.debug("SMTPサーバ[" + host + "]との接続を切断します。");
                try {
                    // SMTPサーバとの接続を切断
                    transport.close();
                } catch (MessagingException e) {
                    log.error("SMTPサーバ[" + host + "]との接続切断に失敗しました。", e);
                    throw new MailException("SMTPサーバ[" + host
                            + "]との接続切断に失敗しました。");
                }
                log.debug("SMTPサーバ[" + host + "]との接続を切断しました。");
            }
        }
    }

    /**
     * 新しいMimeMessageオブジェクトを生成します。<br>
     * messageIdプロパティがセットされている場合、OMLMimeMessageのインスタンスを生成します。
     * 
     * @return 新しいMimeMessageオブジェクト
     */
    private MimeMessage createMimeMessage(Session session) {
        if (isMessageIdCustomized()) {
            return new OMLMimeMessage(session, messageId);
        }
        return new MimeMessage(session);
    }

    /**
     * Message-Idヘッダのドメイン部分を独自にセットしているかどうか判定します。
     * 
     * @return Message-Idヘッダのドメイン部分を独自にセットしている場合 true
     */
    private boolean isMessageIdCustomized() {
        return messageId != null;
    }

    /**
     * エンコーディングに使用する文字コードを返します。
     * 
     * @return エンコーディングに使用する文字コード
     */
    public String getCharset() {
        return charset;
    }

    /**
     * メールの件名や本文のエンコーディングに使用する文字コードを指定します。
     * デフォルトは<code>ISO-2022-JP</code>です。
     * <p>
     * 日本語環境で利用する場合は通常変更する必要はありません。
     * 
     * @param charset エンコーディングに使用する文字コード
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * セットされたSMTPサーバのホスト名、またはIPアドレスを返します。
     * 
     * @return SMTPサーバのホスト名、またはIPアドレス
     */
    public String getHost() {
        return host;
    }

    /**
     * SMTPサーバのホスト名、またはIPアドレスをセットします。
     * デフォルトは localhost です。
     * 
     * @param host SMTPサーバのホスト名、またはIPアドレス
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return SMTPサーバ認証パスワード
     */
    public String getPassword() {
        return password;
    }

    /**
     * SMTPサーバの接続認証が必要な場合にパスワードをセットします。
     * 
     * @param password SMTPサーバ認証パスワード
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return SMTPサーバのポート番号
     */
    public int getPort() {
        return port;
    }

    /**
     * SMTPサーバのポート番号をセットします。
     * 
     * @param port SMTPサーバのポート番号
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * プロトコルを返します。
     * 
     * @return プロトコル
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * プロトコルをセットします。デフォルトは「smtp」。
     * 
     * @param protocol プロトコル
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return Return-Pathアドレス
     */
    public String getReturnPath() {
        return returnPath;
    }

    /**
     * Return-Pathアドレスをセットします。
     * <p>
     * 送信するMailインスタンスに指定されたFromアドレス以外のアドレスをReturn-Pathとしたい場合に使用します。
     * ここでセットされたReturn-Pathより、MailインスタンスにセットされたReturn-Pathが優先されます。
     * 
     * @param returnPath Return-Pathアドレス
     */
    public void setReturnPath(String returnPath) {
        this.returnPath = returnPath;
    }

    /**
     * @return SMTPサーバ認証ユーザ名
     */
    public String getUsername() {
        return username;
    }

    /**
     * SMTPサーバの接続認証が必要な場合にユーザ名をセットします。
     * 
     * @param username SMTPサーバ認証ユーザ名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * SMTPサーバとの接続タイムアウトをセットします。
     * 単位はミリ秒。デフォルトは5,000ミリ秒(5秒)です。
     * <p>
     * -1を指定すると無限大になりますが、お薦めしません。
     * 
     * @since 1.1.4
     * @param connectionTimeout SMTPサーバとの接続タイムアウト
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * SMTPサーバへの送信時のタイムアウトをセットします。
     * 単位はミリ秒。デフォルトは5,000ミリ秒(5秒)です。<br>
     * 送信時にタイムアウトすると、<code>com.ozacc.mail.MailSendException</code>がスローされます。
     * <p>
     * -1を指定すると無限大になりますが、お薦めしません。
     * 
     * @since 1.1.4
     * @param readTimeout SMTPサーバへの送受信時のタイムアウト
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 生成されるMimeMessageに付けられるMessage-Idヘッダのドメイン部分を指定します。<br>
     * 指定されない場合(nullや空文字列の場合)は、JavaMailがMessage-Idヘッダを生成します。
     * JavaMailが生成する「JavaMail.実行ユーザ名@ホスト名」のMessage-Idを避けたい場合に、このメソッドを使用します。
     * <p>
     * messageIdプロパティがセットされている場合、Mailから生成されるMimeMessageのMessage-Idには
     * <code>タイムスタンプ + ランダムに生成される16桁の数値 + ここでセットされた値</code>
     * が使用されます。
     * <p>
     * 生成されるMessage-Idの例。 (実際の数値部分は送信メール毎に変わります)<ul>
     * <li>messageIdに'example.com'を指定した場合・・・1095714924963.5619528074501343@example.com</li>
     * <li>messageIdに'@example.com'を指定した場合・・・1095714924963.5619528074501343@example.com (上と同じ)</li>
     * <li>messageIdに'OML@example.com'を指定した場合・・・1095714924963.5619528074501343.OML@example.com</li>
     * <li>messageIdに'.OML@example.com'を指定した場合・・・1095714924963.5619528074501343.OML@example.com (上と同じ)</li>
     * </ul>
     * <p>
     * <strong>注:</strong> このMessage-Idは<code>send(Mail)</code>か<code>send(Mail[])</code>メソッドが呼びだれた時にのみ有効です。MimeMessageを直接送信する場合には適用されません。
     * 
     * @param messageId メールに付けられるMessage-Idヘッダのドメイン部分
     * @throws IllegalArgumentException @を複数含んだ文字列を指定した場合
     */
    public void setMessageId(String messageId) {
        if (messageId == null || messageId.length() < 1) {
            return;
        }

        String[] parts = messageId.split("@");
        if (parts.length > 2) {
            throw new IllegalArgumentException(
                    "messageIdプロパティに'@'を複数含むことはできません。[" + messageId + "]");
        }

        this.messageId = messageId;
    }

    /**
     * メール送信用のプロパティに追加で設定したい値を設定します。
     * 
     * @param key キー。
     * @param value 値。
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * STARTTLSを有効にするかどうかを返します。
     * 
     * @return STARTTLSを有効にするかどうか
     */
    public boolean isStarttlsEnabled() {
        return starttlsEnabled;
    }

    /**
     * STARTTLSを有効にするかどうかを設定します。
     * 
     * @param enabled STARTTLSを有効にするかどうか
     */
    public void setStarttlsEnabled(boolean enabled) {
        starttlsEnabled = enabled;
    }

    /**
     * MimeMessageインスタンスと、そのメールに対応するReturn-Path、envelope-toアドレスをラップするクラス。
     * 
     * @author Tomohiro Otsuka
     * @version $Id: SendMailImpl.java,v 1.7.2.6 2007/03/30 13:03:44 otsuka Exp $
     */
    private static class MimeMessageWrapper {

        private MimeMessage mimeMessage;

        private String returnPath;

        private InternetAddress[] envelopeTo;

        public MimeMessageWrapper(MimeMessage mimeMessage, String returnPath) {
            this.mimeMessage = mimeMessage;
            this.returnPath = returnPath;
        }

        public MimeMessageWrapper(MimeMessage mimeMessage, String returnPath,
                InternetAddress[] envelopeTo) {
            this.mimeMessage = mimeMessage;
            this.returnPath = returnPath;
            this.envelopeTo = envelopeTo;
        }

        public MimeMessage getMimeMessage() {
            return mimeMessage;
        }

        public String getReturnPath() {
            return returnPath;
        }

        public boolean hasEnvelopeTo() {
            return envelopeTo.length > 0;
        }

        public InternetAddress[] getEnvelopeTo() {
            return envelopeTo;
        }
    }
}