/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.caravan.jaxws.publisher.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages request/response context of incoming HTTP requests using a ThreadLocal.
 */
public final class RequestContext {

  private static ThreadLocal<RequestContext> threadLocal = new ThreadLocal<RequestContext>();

  private final HttpServletRequest request;
  private final HttpServletResponse response;

  /**
   * @param request Request
   * @param response Response
   */
  public RequestContext(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
  }

  /**
   * @return Request
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * @return Response
   */
  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * @return Context for current request
   */
  public static RequestContext getRequestContext() {
    return threadLocal.get();
  }

  public static ThreadLocal<RequestContext> getThreadLocal() {
    return threadLocal;
  }

}
