## About JAX-WS Consumer

Consumes SOAP Services via JAX-WS from OSGi services.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.caravan/io.wcm.caravan.jaxws.consumer/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.caravan/io.wcm.caravan.jaxws.consumer)


### Documentation

* [API documentation][apidocs]
* [Changelog][changelog]


[apidocs]: apidocs/
[changelog]: changes-report.html


### Overview

Consumes SOAP Services via JAX-WS from OSGi services using [Apache CXF][apache-cxf] as JAX-WS implementation.

Features:

- Use generated JAX-WS proxy classes
- Make sure [Apache CXF][apache-cxf] runs smoothly in OSGi container
- Full control over transport configuration e.g. Basic Authentication, Proxy host and authentication, SSL configuration and timeouts
- Possibility to hook into the CXF-internal message processing to customize SOAP communication behavior
- Supports WS-Addressing
- Supports ignoring unexpected elements

Usage example: https://github.com/wcm-io-caravan/caravan-jaxws/tree/develop/integration-test/helloworld-consumer


### Dependencies

You also have to deploy this bundle which wraps [Apache CXF][apache-cxf] including the necessary dependencies:<br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.osgi.wrapper/io.wcm.osgi.wrapper.cxf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.osgi.wrapper/io.wcm.osgi.wrapper.cxf)


[apache-cxf]: http://cxf.apache.org/
