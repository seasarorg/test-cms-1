package org.seasar.cms.framework.creator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class ClassDescBag {

    private Map map_ = new HashMap();

    private Map createdMap_ = new HashMap();

    private Map updatedMap_ = new HashMap();

    private Map failedMap_ = new LinkedHashMap();

    private ClassDescSet set_ = new ClassDescSet();

    public Map getClassDescMap() {
        return getClassDescMap(map_, null);
    }

    public ClassDesc[] getClassDescs() {
        return getClassDescs(map_, null);
    }

    public Map getClassDescMap(String kind) {
        return getClassDescMap(map_, kind);
    }

    public ClassDesc[] getClassDescs(String kind) {
        return (ClassDesc[]) getClassDescMap(kind).values().toArray(
            new ClassDesc[0]);
    }

    public Iterator getClassDescKindIterator() {
        return map_.keySet().iterator();
    }

    public Map getCreatedClassDescMap() {
        return getClassDescMap(createdMap_, null);
    }

    public ClassDesc[] getCreatedClassDescs() {
        return (ClassDesc[]) getCreatedClassDescMap().values().toArray(
            new ClassDesc[0]);
    }

    public Map getCreatedClassDescMap(String kind) {
        return getClassDescMap(createdMap_, kind);
    }

    public ClassDesc[] getCreatedClassDescs(String kind) {
        return (ClassDesc[]) getCreatedClassDescMap(kind).values().toArray(
            new ClassDesc[0]);
    }

    public Map getUpdatedClassDescMap() {
        return getClassDescMap(updatedMap_, null);
    }

    public ClassDesc[] getUpdatedClassDescs() {
        return (ClassDesc[]) getUpdatedClassDescMap().values().toArray(
            new ClassDesc[0]);
    }

    public Map getUpdatedClassDescMap(String kind) {
        return getClassDescMap(updatedMap_, kind);
    }

    public ClassDesc[] getUpdatedClassDescs(String kind) {
        return (ClassDesc[]) getUpdatedClassDescMap(kind).values().toArray(
            new ClassDesc[0]);
    }

    Map getClassDescMap(Map map, String kind) {
        Map got = (Map) map.get(kind);
        if (got == null) {
            got = new LinkedHashMap();
            map.put(kind, got);
        }
        return got;
    }

    ClassDesc[] getClassDescs(Map map, String kind) {
        return (ClassDesc[]) getClassDescMap(map, kind).values().toArray(
            new ClassDesc[0]);
    }

    public ClassDesc[] getFailedClassDescs() {
        return (ClassDesc[]) failedMap_.values().toArray(new ClassDesc[0]);
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

    public void addAsFailed(ClassDesc classDesc) {
        failedMap_.put(classDesc.getName(), classDesc);
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
