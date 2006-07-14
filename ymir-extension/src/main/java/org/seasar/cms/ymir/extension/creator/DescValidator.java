package org.seasar.cms.ymir.creator;

public class DescValidator {

    private DescValidator() {
    }

    public static boolean isValid(ClassDesc classDesc, ClassDescSet classDescSet) {

        PropertyDesc[] pds = classDesc.getPropertyDescs();
        for (int i = 0; i < pds.length; i++) {
            if (!isValid(pds[i], classDescSet)) {
                return false;
            }
        }
        MethodDesc[] mds = classDesc.getMethodDescs();
        for (int i = 0; i < mds.length; i++) {
            if (!isValid(mds[i], classDescSet)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValid(PropertyDesc propertyDesc,
        ClassDescSet classDescSet) {

        return isValid(propertyDesc.getTypeDesc(), classDescSet);
    }

    public static boolean isValid(MethodDesc methodDesc,
        ClassDescSet classDescSet) {

        if (!isValid(methodDesc.getReturnTypeDesc(), classDescSet)) {
            return false;
        }
        ParameterDesc[] parameterDescs = methodDesc.getParameterDescs();
        for (int i = 0; i < parameterDescs.length; i++) {
            if (!isValid(parameterDescs[i], classDescSet)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValid(ParameterDesc parameterDesc,
        ClassDescSet classDescSet) {

        return isValid(parameterDesc.getTypeDesc(), classDescSet);
    }

    public static boolean isValid(TypeDesc typeDesc, ClassDescSet classDescSet) {

        return isValidClassName(typeDesc.getClassDesc().getName(), classDescSet);
    }

    public static boolean isValidClassName(String className,
        ClassDescSet classDescSet) {

        if (TypeDesc.TYPE_VOID.equals(className)) {
            return true;
        } else if (isPrimitive(className)) {
            return true;
        } else if (classDescSet != null && classDescSet.contains(className)) {
            return true;
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = DescValidator.class.getClassLoader();
            }
            try {
                Class.forName(className, false, cl);
                return true;
            } catch (ClassNotFoundException ex) {
                return false;
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
