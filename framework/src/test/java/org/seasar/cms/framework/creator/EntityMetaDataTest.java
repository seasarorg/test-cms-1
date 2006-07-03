package org.seasar.cms.framework.creator;

import java.io.File;

import org.seasar.cms.framework.Request;
import org.seasar.cms.framework.Response;

import junit.framework.TestCase;

public class EntityMetaDataTest extends TestCase {

    private EntityMetaData target_ = new EntityMetaData(new SourceCreator() {

        public String getPagePackageName() {
            return "com.example.web";
        }

        public String getDtoPackageName() {
            return "com.example.dto";
        }

        public String getDaoPackageName() {
            return "com.example.dao";
        }

        public String getDxoPackageName() {
            return "com.example.dxo";
        }

        public String getComponentName(String path, String method) {
            return null;
        }

        public String getActionName(String path, String method) {
            return null;
        }

        public String getClassName(String componentName) {
            return null;
        }

        public File getSourceFile(String className) {
            return null;
        }

        public File getTemplateFile(String className) {
            return null;
        }

        public Response update(String path, String method, Request request) {
            return null;
        }
    }, "com.example.dto.TestDto");

    public void testGetEntityName() throws Exception {

        // ## Arrange ##

        // ## Act ##
        String actual = target_.getEntityName();

        // ## Assert ##
        assertEquals("Test", actual);

    }

    public void testGetDtoClassDesc() throws Exception {

        // ## Arrange ##

        // ## Act ##
        ClassDesc actual = target_.getDtoClassDesc();

        // ## Assert ##
        assertEquals("com.example.dto.TestDto", actual.getName());
    }

    public void testGetBeanClassDesc() throws Exception {

        // ## Arrange ##

        // ## Act ##
        ClassDesc actual = target_.getBeanClassDesc();

        // ## Assert ##
        assertEquals("com.example.dao.Test", actual.getName());
    }

    public void testGetDaoClassDesc() throws Exception {

        // ## Arrange ##

        // ## Act ##
        ClassDesc actual = target_.getDaoClassDesc();

        // ## Assert ##
        assertEquals("com.example.dao.TestDao", actual.getName());
    }

    public void testGetDxoClassDesc() throws Exception {

        // ## Arrange ##

        // ## Act ##
        ClassDesc actual = target_.getDxoClassDesc();

        // ## Assert ##
        assertEquals("com.example.dxo.TestDxo", actual.getName());
    }
}
