## About JAX-WS Publisher

Publishes OSGi services as SOAP services via JAX-WS.

[![Maven Central](https://img.shields.io/maven-central/v/io.wcm.caravan/io.wcm.caravan.jaxws.publisher)](https://repo1.maven.org/maven2/io/wcm/caravan/io.wcm.caravan.jaxws.publisher/)


### Documentation

* [API documentation][apidocs]
* [Changelog][changelog]


[apidocs]: apidocs/
[changelog]: changes-report.html


### Overview

Publishes OSGi services as SOAP services via JAX-WS using [Apache CXF][apache-cxf] as JAX-WS implementation.

Features:

- Use generated JAX-WS proxy classes
- Make sure [Apache CXF][apache-cxf] runs smoothly in OSGi container
- Register JAX-WS interface implementation as Servlet to publish SOAP interface to the outside

Usage example: https://github.com/wcm-io-caravan/caravan-jaxws/tree/develop/integration-test/helloworld-publisher


### Dependencies

You also have to deploy this bundle which wraps [Apache CXF][apache-cxf] including the necessary dependencies:<br/>
[![Maven Central](https://img.shields.io/maven-central/v/io.wcm.osgi.wrapper/io.wcm.osgi.wrapper.cxf)](https://repo1.maven.org/maven2/io/wcm/osgi/wrapper/io.wcm.osgi.wrapper.cxf)


[apache-cxf]: http://cxf.apache.org/
