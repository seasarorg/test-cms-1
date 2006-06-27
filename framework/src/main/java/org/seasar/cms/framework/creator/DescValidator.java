package org.seasar.cms.framework.creator;

public class DescValidator {

    private static final String PACKAGE_JAVA_LANG = "java.lang.";

    private DescValidator() {
    }

    public static boolean isValid(ClassDesc classDesc, SourceCreator creator) {

        PropertyDesc[] pds = classDesc.getPropertyDescs();
        for (int i = 0; i < pds.length; i++) {
            if (!isValid(pds[i], creator)) {
                return false;
            }
        }
        MethodDesc[] mds = classDesc.getMethodDescs();
        for (int i = 0; i < mds.length; i++) {
            if (!isValid(mds[i], creator)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValid(PropertyDesc propertyDesc,
        SourceCreator creator) {

        return isValid(propertyDesc.getTypeDesc(), creator);
    }

    public static boolean isValid(MethodDesc methodDesc, SourceCreator creator) {

        if (!isValid(methodDesc.getReturnTypeDesc(), creator)) {
            return false;
        }
        TypeDesc[] parameterTypeDescs = methodDesc.getParameterTypeDescs();
        for (int i = 0; i < parameterTypeDescs.length; i++) {
            if (!isValid(parameterTypeDescs[i], creator)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValid(TypeDesc typeDesc, SourceCreator creator) {

        String className = typeDesc.getComponentName();
        if (TypeDesc.TYPE_VOID.equals(className)) {
            return true;
        } else if (isPrimitive(className)) {
            return true;
        } else {
            if (className.indexOf('.') < 0) {
                className = PACKAGE_JAVA_LANG + className;
            }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = DescValidator.class.getClassLoader();
            }
            try {
                Class.forName(className, false, cl);
                return true;
            } catch (ClassNotFoundException ex) {
                return creator.getSourceFile(className).exists();
            }
        }
    }

    static boolean isPrimitive(String name) {

        return ("byte".equals(name) || "short".equals(name)
            || "int".equals(name) || "long".equals(name)
            || "float".equals(name) || "double".equals(name)
            || "char".equals(name) || "boolean".equals(name));
    }
}
