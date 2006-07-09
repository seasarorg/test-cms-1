package org.seasar.cms.ymir;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.seasar.cms.ymir.impl.FormFileImpl;

/**
 * Commons FileUploadを使ってマルチパートリクエストを扱うようにした
 * HttpServletRequestクラスです。
 * <p><b>同期化：</b>
 * このクラスはスレッドセーフではありません。
 * </p>
 * 
 * @author YOKOTA Takehiko
 */
public class MultipartServletRequest extends HttpServletRequestWrapper {

    public static final String ATTR_FORMFILEMAP = MultipartServletRequest.class
        .getName()
        + ".formFileMap";

    private HttpServletRequest request_;

    private Map paramMap_;

    public MultipartServletRequest(HttpServletRequest request) {

        super(request);
        request_ = request;

        if (ServletFileUpload.isMultipartContent(new ServletRequestContext(
            request))) {
            String encoding = request.getCharacterEncoding();
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding(encoding);
            try {
                paramMap_ = prepareParameters(upload.parseRequest(request),
                    encoding);
            } catch (FileUploadException ex) {
                paramMap_ = request.getParameterMap();
            }
        } else {
            paramMap_ = request.getParameterMap();
        }
    }

    /*
     * HttpServletRequest
     */

    public String getParameter(String name) {
        String value = (String) getFirst(paramMap_.get(name));
        if (value == null) {
            value = super.getParameter(name);
        }
        return value;
    }

    public Map getParameterMap() {
        return paramMap_;
    }

    public Enumeration getParameterNames() {
        return Collections.enumeration(paramMap_.keySet());
    }

    public String[] getParameterValues(String name) {
        return (String[]) paramMap_.get(name);
    }

    /*
     * private scope methods
     */

    private Map prepareParameters(List fileItemList, String encoding) {
        Map paramMap = new HashMap(request_.getParameterMap());
        Map fileMap = new HashMap();
        for (Iterator itr = fileItemList.iterator(); itr.hasNext();) {
            FileItem fileItem = (FileItem) itr.next();
            String fieldName = fileItem.getFieldName();
            if (fileItem.isFormField()) {
                String value;
                try {
                    value = fileItem.getString(encoding);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("Can't happen!");
                }
                paramMap.put(fieldName, add(paramMap.get(fieldName), value,
                    String.class));
            } else {
                FormFile value = new FormFileImpl(fileItem);
                fileMap.put(fieldName, add(fileMap.get(fieldName), value,
                    FormFile.class));
            }
        }
        request_.setAttribute(ATTR_FORMFILEMAP, Collections
            .unmodifiableMap(fileMap));

        return Collections.unmodifiableMap(paramMap);
    }

    private Object add(Object obj, Object value, Class clazz) {
        Object[] newObjs;
        if (obj == null) {
            newObjs = (Object[]) Array.newInstance(clazz, 1);
            newObjs[0] = value;
        } else {
            Object[] objs = (Object[]) obj;
            newObjs = (Object[]) Array.newInstance(obj.getClass()
                .getComponentType(), objs.length + 1);
            System.arraycopy(objs, 0, newObjs, 0, objs.length);
            newObjs[objs.length] = value;
        }
        return newObjs;
    }

    private Object getFirst(Object obj) {
        Object[] objs = (Object[]) obj;
        if (objs == null || objs.length == 0) {
            return null;
        } else {
            return objs[0];
        }
    }
}
