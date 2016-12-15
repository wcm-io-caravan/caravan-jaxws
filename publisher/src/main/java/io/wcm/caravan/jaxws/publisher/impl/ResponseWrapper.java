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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Response wrapper
 */
public final class ResponseWrapper extends HttpServletResponseWrapper {

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * @param response Response
   */
  public ResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public void flushBuffer() throws IOException {
    try {
      super.flushBuffer();
    }
    catch (IOException ex) {
      // Suppress exception concerning closed streams during buffer flushing of SOAP server requests
      // (buffer is flushed automatically on close).
      log.debug("IO Exception occured during buffer flushing, suppressing it.", ex);
    }
  }

}
