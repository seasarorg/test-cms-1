package org.seasar.cms.ymir.extension.creator.action;

import org.seasar.cms.ymir.Request;
import org.seasar.cms.ymir.Response;
import org.seasar.cms.ymir.extension.creator.PathMetaData;

import junit.framework.TestCase;

public class UpdateActionSelectorTest extends TestCase {

    private UpdateActionSelector target_ = new UpdateActionSelector();

    public void testGetAction() throws Exception {
        UpdateAction updateAction = new UpdateAction() {
            public Response act(Request request, PathMetaData pathMetaData) {
                return null;
            }
        };
        target_.register(new Condition(State.ANY, State.ANY, State.ANY, null),
            updateAction);

        assertSame(updateAction, target_.getAction(new Condition(State.FALSE,
            State.FALSE, State.FALSE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.FALSE,
            State.FALSE, State.TRUE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.FALSE,
            State.TRUE, State.FALSE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.FALSE,
            State.TRUE, State.TRUE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.TRUE,
            State.FALSE, State.FALSE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.TRUE,
            State.FALSE, State.TRUE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.TRUE,
            State.TRUE, State.FALSE, null)));
        assertSame(updateAction, target_.getAction(new Condition(State.TRUE,
            State.TRUE, State.TRUE, null)));
        assertNull(target_.getAction(new Condition(State.ANY, State.ANY,
            State.ANY, null)));
    }
}
