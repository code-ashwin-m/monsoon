package org.monsoon.framework.web.autoconfigure;

import org.monsoon.framework.web.interfaces.ReqResHelper;

import java.io.InputStream;

public class DefaultReqResHelper implements ReqResHelper {
    @Override
    public Object readValue(InputStream bodyStream, Class<?> type) throws Exception {
        return type.getConstructor().newInstance();
    }

    @Override
    public String writeValueAsString(Object result) throws Exception {
        return "";
    }
}
