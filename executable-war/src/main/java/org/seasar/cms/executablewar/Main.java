package org.seasar.cms.executablewar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 実行可能WARファイルのためのランチャクラスです。
 * <p><a href="http://hudson.dev.java.net/">Hudson</a>のexecutable-warモジュールの
 * Mainクラスのコードをベースにしています。
 * </p>
 *
 * @author YOKOTA Takehiko
 */
public class Main {
    private static final String EXECUTABLEWAR_HOME = "EXECUTABLEWAR_HOME";

    public static void main(String[] args) throws Exception {
        // ウィンドウシステムが動作していない環境でもJava2Dのライブラリを利用できるようにします。
        System.setProperty("java.awt.headless", "true");

        File me = whoAmI();

        // WinstoneのjarファイルのURLを取得します。
        URL jar = Main.class.getClassLoader().getResource("winstone.jar");

        // Winstoneを動作させるためにJarファイルをファイルシステムに配置します。
        File tmpJar;
        try {
            tmpJar = File.createTempFile("winstone", "jar");
        } catch (IOException e) {
            String tmpdir = System.getProperty("java.io.tmpdir");
            IOException x = new IOException("Failed to create a temporary file in " + tmpdir);
            x.initCause(e);
            throw x;
        }
        copyStream(jar.openStream(), new FileOutputStream(tmpJar));
        tmpJar.deleteOnExit();

        // 誤動作の原因になるので、以前展開したWARのコピーを削除しておきます。
        File tempFile = File.createTempFile("dummy", "dummy");
        deleteContents(new File(tempFile.getParent(), "winstone/" + me.getName()));
        tempFile.delete();

        // Winstoneのランチャのmainメソッドを取得します。
        ClassLoader cl = new URLClassLoader(new URL[] { tmpJar.toURL() });
        Class<?> launcher = cl.loadClass("winstone.Launcher");
        Method mainMethod = launcher.getMethod("main", new Class[] { String[].class });

        // 引数をfigure out the arguments
        List<String> arguments = new ArrayList<String>(Arrays.asList(args));
        arguments.add(0, "--warfile=" + me.getAbsolutePath());
        if (!hasWebRoot(arguments))
            // テンポラリディレクトリに展開したコンテンツがcronジョブによって削除されてしまうため、
            // webrootが指定されていない場合でもwebrootを設定するようにします。
            arguments.add("--webroot=" + new File(getHomeDir(), me.getName()));

        // winstoneを実行します。
        mainMethod.invoke(null, new Object[] { arguments.toArray(new String[0]) });
    }

    private static boolean hasWebRoot(List<String> arguments) {
        for (Iterator<String> itr = arguments.iterator(); itr.hasNext();) {
            String s = itr.next();
            if (s.startsWith("--webroot="))
                return true;
        }
        return false;
    }

    /**
     * Figures out the URL of <tt>hudson.war</tt>.
     */
    public static File whoAmI() throws IOException, URISyntaxException {
        URL classFile = Main.class.getClassLoader()
                .getResource(Main.class.getName().replace('.', '/').concat(".class"));

        // JNLPはローカルにキャッシュされているjarのURLではなく、jarがもともと置かれていた場所
        // （http://hudson.dev.java.net/... のような）のURLを返してしまいます。
        // そのためローカルのJARファイルの名前を得るためにこのような遠回りのアプローチをとる必要があります。
        return new File(((JarURLConnection) classFile.openConnection()).getJarFile().getName());
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    private static void deleteContents(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {// 念のため。
                for (int i = 0; i < files.length; i++)
                    deleteContents(files[i]);
            }
        }
        file.delete();
    }

    /**
     * ホームディレクトリを決定して返します。
     *
     * 設定を良く間違えられるため、{@link String#trim()}をして問題がなくなるようにしています。
     */
    private static File getHomeDir() {
        // 最初にJNDIをチェックします。
        try {
            InitialContext iniCtxt = new InitialContext();
            Context env = (Context) iniCtxt.lookup("java:comp/env");
            String value = (String) env.lookup(EXECUTABLEWAR_HOME);
            if (value != null && value.trim().length() > 0)
                return new File(value.trim());
            // もう一箇所チェックします。詳しくはHudsonのissue #1314を参照して下さい。
            value = (String) iniCtxt.lookup(EXECUTABLEWAR_HOME);
            if (value != null && value.trim().length() > 0)
                return new File(value.trim());
        } catch (NamingException e) {
            // 無視します。
        }

        // 最後にシステムプロパティをチェックします。
        String sysProp = System.getProperty(EXECUTABLEWAR_HOME);
        if (sysProp != null)
            return new File(sysProp.trim());

        // 次に環境変数を見ます。
        try {
            String env = System.getenv(EXECUTABLEWAR_HOME);
            if (env != null)
                return new File(env.trim()).getAbsoluteFile();
        } catch (Throwable _) {
            // このコードがJDK1.4で動作している場合、このメソッド呼び出しが失敗しても
            // 次のメソッドに処理がうまく移らないのでこうしています。
        }

        // 自分自身で場所を決めます。

        // ホームディレクトリを利用します。
        return new File(new File(System.getProperty("user.home")), ".executablewar");
    }
}
