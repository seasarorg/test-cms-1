package org.seasar.cms.ymir;

public interface Updater {

    Response update(Request request, Response response);

    String filterResponse(String response);

    Response updateByException(Request request, Throwable t);
}
