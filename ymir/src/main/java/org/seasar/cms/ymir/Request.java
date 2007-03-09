package org.seasar.cms.ymir;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public interface Request extends AttributeContainer {

    String METHOD_CONNECT = "CONNECT";

    String METHOD_DELETE = "DELETE";

    String METHOD_GET = "GET";

    String METHOD_HEAD = "HEAD";

    String METHOD_LINK = "LINK";

    String METHOD_OPTIONS = "OPTIONS";

    String METHOD_PATCH = "PATCH";

    String METHOD_POST = "POST";

    String METHOD_PUT = "PUT";

    String METHOD_TRACE = "TRACE";

    String METHOD_UNLINK = "UNLINK";

    String DISPATCHER_REQUEST = "REQUEST";

    String DISPATCHER_FORWARD = "FORWARD";

    String DISPATCHER_INCLUDE = "INCLUDE";

    String DISPATCHER_ERROR = "ERROR";

    /**
     * コンテキストパスを返します。
     *
     * @return コンテキストパス。
     */
    String getContextPath();

    /**
     * パスを返します。
     * <p>返されるパスはコンテキストパス相対です。</p>
     *
     * @return パス。
     */
    String getPath();

    /**
     * 絶対パスを返します。
     * <p>返されるパスはコンテキストパスとパスを連結したものです。</p>
     *
     * @return
     */
    String getAbsolutePath();

    /**
     * HTTPメソッドを返します。
     * <p>返されるメソッド名は全て大文字です。</p>
     *
     * @return メソッド名。
     */
    String getMethod();

    /**
     * ディスパッチャ名を返します。
     * <p>返されるディスパッチャ名は{@link Request#DISPATCHER_REQUEST}、
     * {@link Request#DISPATCHER_FORWARD}、
     * {@link Request#DISPATCHER_INCLUDE}、
     * {@link Request#DISPATCHER_ERROR}のいずれかです。
     * </p>
     *
     * @return ディスパッチャ名。
     */
    String getDispatcher();

    /**
     * 指定された名前のリクエストパラメータの値を返します。
     * <p>指定された名前のリクエストパラメータが存在しない場合はnullを返します。
     * </p>
     * <p>ファイルパラメータは対象外です。</p>
     *
     * @param name リクエストパラメータ名。
     * @return 値。
     */
    String getParameter(String name);

    /**
     * 指定された名前のリクエストパラメータの値を返します。
     * <p>指定された名前のリクエストパラメータが存在しない場合は
     * <code>defaultValue</code>を返します。
     * </p>
     * <p>ファイルパラメータは対象外です。</p>
     *
     * @param name リクエストパラメータ名。
     * @param defaultValue デフォルトの値。
     * @return 値。
     */
    String getParameter(String name, String defaultValue);

    /**
     * 指定された名前のリクエストパラメータの値を返します。
     * <p>指定された名前のリクエストパラメータが存在しない場合はnullを返します。
     * </p>
     * <p>ファイルパラメータは対象外です。</p>
     *
     * @param name リクエストパラメータ名。
     * @return 値。
     */
    String[] getParameterValues(String name);

    /**
     * 指定された名前のリクエストパラメータの値を返します。
     * <p>指定された名前のリクエストパラメータが存在しない場合は
     * <code>defaultValues</code>を返します。
     * </p>
     * <p>ファイルパラメータは対象外です。</p>
     *
     * @param name リクエストパラメータ名。
     * @param defaultValues デフォルトの値。
     * @return 値。
     */
    String[] getParameterValues(String name, String[] defaultValues);

    /**
     * 全てのリクエストパラメータの名前のIteratorを返します。
     * <p>ファイルパラメータは対象外です。</p>
     *
     * @return Iteratorオブジェクト。
     */
    Iterator getParameterNames();

    /**
     * 全てのリクエストパラメータを格納しているMapを返します。
     * <p>Mapのキーはパラメータ名（String）、値はパラメータの値（String[]）です。
     * </p>
     * <p>ファイルパラメータは対象外です。</p>
     *
     * @return Mapオブジェクト。
     */
    Map getParameterMap();

    /**
     * &lt;input type="file" /&gt;タグで指定されたファイルパラメータのうち、
     * 指定された名前を持つものを返します。
     * <p>指定された名前のファイルパラメータが存在しない場合はnullを返します。
     * </p>
     *
     * @param name ファイルパラメータ名。
     * @return 値。
     */
    FormFile getFileParameter(String name);

    /**
     * &lt;input type="file" /&gt;タグで指定されたファイルパラメータのうち、
     * 指定された名前を持つものを返します。
     * <p>指定された名前のファイルパラメータが存在しない場合はnullを返します。
     * </p>
     *
     * @param name ファイルパラメータ名。
     * @return 値。
     */
    FormFile[] getFileParameterValues(String name);

    /**
     * 全てのファイルパラメータの名前のIteratorを返します。
     *
     * @return Iteratorオブジェクト。
     */
    Iterator getFileParameterNames();

    /**
     * 全てのファイルパラメータを格納しているMapを返します。
     * <p>Mapのキーはパラメータ名（String）、値はパラメータの値（FormFile[]）です。
     * </p>
     *
     * @return Mapオブジェクト。
     */
    Map getFileParameterMap();

    /**
     * リクエストスコープのオブジェクトを保持するためのAttributeContainerを返します。
     *
     * @return AttributeContainerオブジェクト。
     */
    AttributeContainer getAttributeContainer();

    /**
     * リクエストパスに対応するコンポーネントの名前を返します。
     * <p>パスに対応するコンポーネントが存在しない場合はnullを返します。</p>
     *
     * @return コンポーネント名。
     */
    String getComponentName();

    /**
     * リクエストパスとHTTPメソッドに対応するアクションの名前を返します。
     * <p>パスに対応するコンポーネントが存在しない場合や、
     * リクエストパスとHTTPメソッドに対応するアクションが存在しない場合はnullを返します。</p>
     *
     * @return アクション名。
     */
    String getActionName();

    /**
     * リクエストパスに連結されているpathInfo情報を返します。
     *
     * @return pathInfo情報。PathMappingルールによってはnullが返されることもあります。
     */
    String getPathInfo();

    /**
     * アクションのデフォルトの返り値を返します。
     * <p>リクエストを処理した結果構築されたResponseが「パススルー」タイプであった場合に
     * 処理を遷移させる先を表すオブジェクトを返します。</p>
     * <p>デフォルトの返り値がnullでない場合でかつ
     * リクエストを処理した結果構築されたResponseが「パススルー」タイプであった場合に、
     * ResponseConstructorを使ってこのデフォルトの返り値から構築したResponseオブジェクトが
     * 最終的なResponseオブジェクトとしてフレームワークによって利用されます。
     * <p>パスに対応するコンポーネントが存在しない場合や
     * PathMappingルールにデフォルトの返り値が設定されていない場合はnullを返します。</p>
     *
     * @return デフォルトの返り値。
     */
    Object getDefaultReturnValue();

    /**
     * リクエストパラメータによるディスパッチを行なうかどうかを返します。
     * <p>このメソッドの返り値がtrueの場合、
     * コンポーネントが持つメソッドのうち、
     * アクション名とリクエストパラメータを
     * 「<code>_</code>」で連結したものと同じ名前のメソッドが呼び出されます。
     * 例えばコンポーネントのメソッドとして「<code>_post_update</code>」という名前のものと
     * 「<code>_post_replace</code>」という名前のものがある場合、
     * リクエストに対応するアクション名が「<code>_post</code>」でかつ
     * リクエストパラメータに「<code>update</code>」というものが含まれている場合は
     * 「<code>_post_update</code>」が呼び出されます。
     * （なお、「<code>_post_XXXX</code>」形式のメソッドが存在しない場合は
     * 「<code>_post</code>」メソッドが呼び出されます。）
     * </p>
     *
     * @return リクエストパラメータによるディスパッチを行なうかどうか。
     */
    boolean isDispatchingByParameter();

    String extractParameterName(String name);

    /**
     * 現在のリクエストがどのロケールに基づいて処理されているかを返します。
     *
     * @return ロケール。nullを返すことはありません。
     */
    Locale getLocale();

    /**
     * リクエストパスがパスマッピングにマッチしたかどうかを返します。
     *
     * @return リクエストパスがパスマッピングにマッチしたかどうか。
     */
    boolean isMatched();
}
