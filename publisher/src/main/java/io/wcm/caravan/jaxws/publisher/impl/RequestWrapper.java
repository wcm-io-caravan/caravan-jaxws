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
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper that maps all pathinfo to a virtual path, to whom the SOAP services are registered to.
 */
public final class RequestWrapper extends HttpServletRequestWrapper {

  /**
   * Virtual path
   */
  public static final String VIRTUAL_PATH = "/virtualpath";

  private final String virtualPath;

  /**
   * @param request Request
   */
  public RequestWrapper(HttpServletRequest request) {
    super(request);

    String queryString = request.getQueryString();
    virtualPath = VIRTUAL_PATH + (queryString != null ? "?" + queryString : "");
  }

  @Override
  public String getPathInfo() {
    return virtualPath;
  }

  @Override
  public String getRequestURI() {
    return virtualPath;
  }

  @Override
  public String getContextPath() {
    // simulate default root even if container is running at another path
    return "";
  }

}
