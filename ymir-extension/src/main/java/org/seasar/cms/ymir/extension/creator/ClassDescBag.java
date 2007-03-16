package org.seasar.cms.ymir.extension.creator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassDescBag {

    private Map<String, Map<String, ClassDesc>> map_ = new HashMap<String, Map<String, ClassDesc>>();

    private Map<String, Map<String, ClassDesc>> createdMap_ = new HashMap<String, Map<String, ClassDesc>>();

    private Map<String, Map<String, ClassDesc>> updatedMap_ = new HashMap<String, Map<String, ClassDesc>>();

    private Map<String, Map<String, ClassDesc>> failedMap_ = new HashMap<String, Map<String, ClassDesc>>();

    private ClassDescSet set_ = new ClassDescSet();

    private Map<String, String[]> failedClassDescLackingClassNamesMap_ = new HashMap<String, String[]>();

    public Map<String, ClassDesc> getClassDescMap() {
        return getClassDescMap(map_, null);
    }

    public ClassDesc[] getClassDescs() {
        return getClassDescs(map_, null);
    }

    public Map<String, ClassDesc> getClassDescMap(String kind) {
        return getClassDescMap(map_, kind);
    }

    public ClassDesc[] getClassDescs(String kind) {
        return getClassDescMap(kind).values().toArray(new ClassDesc[0]);
    }

    public Iterator getClassDescKindIterator() {
        return map_.keySet().iterator();
    }

    public Map<String, ClassDesc> getCreatedClassDescMap() {
        return getClassDescMap(createdMap_, null);
    }

    public ClassDesc[] getCreatedClassDescs() {
        return getCreatedClassDescMap().values().toArray(new ClassDesc[0]);
    }

    public Map<String, ClassDesc> getCreatedClassDescMap(String kind) {
        return getClassDescMap(createdMap_, kind);
    }

    public ClassDesc[] getCreatedClassDescs(String kind) {
        return getCreatedClassDescMap(kind).values().toArray(new ClassDesc[0]);
    }

    public Map<String, ClassDesc> getUpdatedClassDescMap() {
        return getClassDescMap(updatedMap_, null);
    }

    public ClassDesc[] getUpdatedClassDescs() {
        return getUpdatedClassDescMap().values().toArray(new ClassDesc[0]);
    }

    public Map<String, ClassDesc> getUpdatedClassDescMap(String kind) {
        return getClassDescMap(updatedMap_, kind);
    }

    public ClassDesc[] getUpdatedClassDescs(String kind) {
        return getUpdatedClassDescMap(kind).values().toArray(new ClassDesc[0]);
    }

    public Map<String, ClassDesc> getFailedClassDescMap() {
        return getClassDescMap(failedMap_, null);
    }

    public ClassDesc[] getFailedClassDescs() {
        return getFailedClassDescMap().values().toArray(new ClassDesc[0]);
    }

    public Map<String, ClassDesc> getFailedClassDescMap(String kind) {
        return getClassDescMap(failedMap_, kind);
    }

    public ClassDesc[] getFailedClassDescs(String kind) {
        return getFailedClassDescMap(kind).values().toArray(new ClassDesc[0]);
    }

    public Map<String, String[]> getFailedClassDescLackingClassNamesMap() {
        return failedClassDescLackingClassNamesMap_;
    }

    public String[] getFailedClassDescLackingClassNames(String className) {
        String[] classNames = failedClassDescLackingClassNamesMap_
                .get(className);
        if (classNames != null) {
            return classNames;
        } else {
            return new String[0];
        }
    }

    Map<String, ClassDesc> getClassDescMap(
            Map<String, Map<String, ClassDesc>> map, String kind) {
        Map<String, ClassDesc> got = map.get(kind);
        if (got == null) {
            got = new LinkedHashMap<String, ClassDesc>();
            map.put(kind, got);
        }
        return got;
    }

    ClassDesc[] getClassDescs(Map<String, Map<String, ClassDesc>> map,
            String kind) {
        return getClassDescMap(map, kind).values().toArray(new ClassDesc[0]);
    }

    public ClassDescSet getClassDescSet() {
        return set_;
    }

    public void addAsCreated(ClassDesc classDesc) {
        getClassDescMap().put(classDesc.getName(), classDesc);
        getClassDescMap(classDesc.getKind())
                .put(classDesc.getName(), classDesc);
        getCreatedClassDescMap().put(classDesc.getName(), classDesc);
        getCreatedClassDescMap(classDesc.getKind()).put(classDesc.getName(),
                classDesc);
        set_.add(classDesc);
    }

    public void addAsUpdated(ClassDesc classDesc) {
        getClassDescMap().put(classDesc.getName(), classDesc);
        getClassDescMap(classDesc.getKind())
                .put(classDesc.getName(), classDesc);
        getUpdatedClassDescMap().put(classDesc.getName(), classDesc);
        getUpdatedClassDescMap(classDesc.getKind()).put(classDesc.getName(),
                classDesc);
        set_.add(classDesc);
    }

    public void addAsFailed(ClassDesc classDesc, String[] lackingClassNames) {
        getClassDescMap().put(classDesc.getName(), classDesc);
        getClassDescMap(classDesc.getKind())
                .put(classDesc.getName(), classDesc);
        getFailedClassDescMap().put(classDesc.getName(), classDesc);
        getFailedClassDescMap(classDesc.getKind()).put(classDesc.getName(),
                classDesc);
        getFailedClassDescLackingClassNamesMap().put(classDesc.getName(),
                lackingClassNames);
    }

    public void remove(String className) {
        remove(map_, className);
        remove(createdMap_, className);
        remove(updatedMap_, className);
        set_.remove(className);
    }

    void remove(Map map, String className) {
        for (Iterator itr = map.values().iterator(); itr.hasNext();) {
            Map kindMap = (Map) itr.next();
            kindMap.remove(className);
        }
    }

    public boolean isEmpty() {
        return map_.isEmpty();
    }
}
