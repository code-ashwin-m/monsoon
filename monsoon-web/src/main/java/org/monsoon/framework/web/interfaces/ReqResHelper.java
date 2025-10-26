package org.monsoon.framework.web.interfaces;

import java.io.InputStream;

public interface ReqResHelper {
    Object readValue(InputStream bodyStream, Class<?> type) throws Exception;
    String writeValueAsString(Object result) throws Exception;
}
