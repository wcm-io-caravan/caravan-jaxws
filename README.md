<img src="https://wcm.io/images/favicon-16@2x.png"/> wcm.io Caravan JAX-WS
======
[![Build Status](https://travis-ci.com/wcm-io-caravan/caravan-jaxws.png?branch=develop)](https://travis-ci.com/wcm-io-caravan/caravan-jaxws)

wcm.io Caravan - JSON Data Pipelining Infrastructure

![Caravan](https://caravan.wcm.io/images/caravan.gif)

Consume and Publish SOAP services in OSGi services via JAX-WS.

Documentation: https://caravan.wcm.io/jaxws/<br/>
Issues: https://wcm-io.atlassian.net/<br/>
Wiki: https://wcm-io.atlassian.net/wiki/<br/>
Continuous Integration: https://travis-ci.com/wcm-io-caravan/caravan-jaxws/<br/>
Commercial support: https://wcm.io/commercial-support.html


## Build from sources

If you want to build wcm.io from sources make sure you have configured all [Maven Repositories](https://caravan.wcm.io/maven.html) in your settings.xml.

See [Travis Maven settings.xml](https://github.com/wcm-io-caravan/caravan-jaxws/blob/master/.travis.maven-settings.xml) for an example with a full configuration.

Then you can build using

```
mvn clean install
```
