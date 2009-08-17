package org.seasar.cms.mailsender;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.seasar.extension.unit.S2TestCase;

import com.ozacc.mail.Mail;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class MailsenderITest extends S2TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        include(getClass().getName().replace('.', '/').concat("_test.dicon"));
    }

    private HoeSendMail getHoeSendMail() {
        return (HoeSendMail) getComponent(HoeSendMail.class);
    }

    private HoeMailsender getTarget() {
        return (HoeMailsender) getComponent(HoeMailsender.class);
    }

    private String expected(String name) throws IOException {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream(
                        getClass().getName().replace('.', '/').concat("_")
                                .concat(name));
        try {
            Reader reader = new InputStreamReader(is, "UTF-8");
            StringWriter sw = new StringWriter();
            char[] buf = new char[4096];
            int len;
            while ((len = reader.read(buf)) >= 0) {
                sw.write(buf, 0, len);
            }
            return sw.toString();
        } finally {
            is.close();
        }
    }

    public void test1_メールが正しく送信できること() throws Exception {
        Mail mail = new Mail();
        mail.setText("text");

        getTarget().send(mail);

        Mail[] actual = getHoeSendMail().getMails();
        assertNotNull(actual);
        assertEquals(1, actual.length);
        assertSame(mail, actual[0]);
        assertEquals("text", mail.getText());
    }

    public void test2_複数のメールが正しく送信できること() throws Exception {
        Mail mail1 = new Mail();
        Mail mail2 = new Mail();
        Mail mail3 = new Mail();
        Mail mail4 = new Mail();
        Mail mail5 = new Mail();

        getTarget()
                .send(
                        mail1,
                        new Mail[] { mail2, mail3 },
                        new ArrayList<Mail>(Arrays.asList(new Mail[] { mail4,
                                mail5 })));

        Mail[] actual = getHoeSendMail().getMails();
        assertNotNull(actual);
        assertEquals(5, actual.length);
        int idx = 0;
        assertSame(mail1, actual[idx++]);
        assertSame(mail2, actual[idx++]);
        assertSame(mail3, actual[idx++]);
        assertSame(mail4, actual[idx++]);
        assertSame(mail5, actual[idx++]);
    }

    public void test3_throws_MailsenderException_がついている場合はメール送信に失敗した場合はMailsenderExceptionがスローされること()
            throws Exception {
        getHoeSendMail().setThrowingException(true);

        try {
            getTarget().send(new Mail());
            fail();
        } catch (MailsenderException expected) {
        }
    }

    public void test4_concreteメソッドの場合はインターセプタが何もしないこと() throws Exception {
        getTarget().send();

        assertNull(getHoeSendMail().getMails());
    }

    public void test5_Mailオブジェクトが引数になく返り値がStringでBodyTemplateアノテーションが付与されている場合は単にテンプレートを評価すること()
            throws Exception {
        HoeDto hoeDto = new HoeDto();
        hoeDto.setMessage("OK");
        hoeDto.setDate(new Date(0L));

        assertEquals(expected("test5_expected.txt"), getTarget().evaluateBody(
                hoeDto));
    }

    public void test6_Configurationオブジェクトが引数にある場合はテンプレートの評価時にそれが使われること()
            throws Exception {
        HoeDto hoeDto = new HoeDto();
        hoeDto.setMessage("お客");
        hoeDto.setDate(new Date(0L));
        Configuration cfg = new Configuration();
        cfg.setDateFormat("yyyyMMdd");
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));
        cfg.setDefaultEncoding("UTF-8");

        getTarget().sendToCustomer(new Mail(), hoeDto, cfg);

        assertEquals("件名", getHoeSendMail().getMails()[0].getSubject());
        assertEquals(expected("test6_expected.txt"), getHoeSendMail()
                .getMails()[0].getText());
    }

    public void test7_テンプレートを評価した結果が件名・ボディであるようなメールが正しく送信されること()
            throws Exception {
        HoeDto hoeDto = new HoeDto();
        hoeDto.setMessage("お客");
        hoeDto.setDate(new Date(0L));

        getTarget().sendToCustomer(new Mail(), hoeDto);

        assertEquals("件名：お客", getHoeSendMail().getMails()[0].getSubject());
    }

    public void test8_複数のメールが正しく送信できること() throws Exception {
        Mail mail1 = new Mail();
        Mail mail2 = new Mail();

        getTarget().send(mail1, mail2);

        Mail[] actual = getHoeSendMail().getMails();
        assertNotNull(actual);
        assertEquals(2, actual.length);
        int idx = 0;
        assertSame(mail1, actual[idx++]);
        assertSame(mail2, actual[idx++]);
    }

    public void test9_件名のテンプレートを指定した場合に正しく件名が設定されること() throws Exception {
        HoeDto hoeDto = new HoeDto();
        hoeDto.setMessage("メッセージ");

        assertEquals("件名：メッセージ", getTarget().evaluateSubject(hoeDto));
    }
}
