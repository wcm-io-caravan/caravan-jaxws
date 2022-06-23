## About JAX-WS Consumer HTTP Client

Bridges Caravan Commons HTTP Client Configuration to JAX-WS Consumer configuration.

[![Maven Central](https://img.shields.io/maven-central/v/io.wcm.caravan/io.wcm.caravan.jaxws.consumer-httpclient)](https://repo1.maven.org/maven2/io/wcm/caravan/io.wcm.caravan.jaxws.consumer-httpclient/)


### Documentation

* [API documentation][apidocs]
* [Changelog][changelog]


[apidocs]: apidocs/
[changelog]: changes-report.html


### Overview

The [Caravan Commons Http Client][caravan-commons-httpclient] provides an OSGi factory configuration to configure HTTP transport properties like Basic Authentication, Proxy host and authentication, SSL configuration and timeouts to all HTTP communications matching a certain host, path or WS Addressing To URI.

With this bundle it is possible to map those configuration also to the HTTP communication for the [JAX-WS Consumer][jaxws-consumer]. Please note that only the configuration is applied, the Apache Commons HTTP Client itself is not used but the HTTP implementation of Apache CXF.


[jaxws-consumer]: ../consumer/
[caravan-commons-httpclient]: https://caravan.wcm.io/commons/httpclient/
