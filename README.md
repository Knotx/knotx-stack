[![][travis img]][travis]
[![][license img]][license]

Knotx stack builder
========

The Knot.x stack : Knot.x + Vert.x & common dependencies


## Debug resolver
Enable TRACE level logging on your logback.xml
```xml
<logger name="knotx-stack-resolver" level="TRACE"/>
```

## Working with snapshots
`-Dknotx.maven.snapshotPolicy=0`


### Docker

To build docker images, you need docker1.3+ and run `mvn clean install`
It builds the 2 main images :
- knotx base - the base image provisionning the knotx appplication (`knotx/knotx`)
- knotx base alpine - the base image based on Java alpine image (`knotx/knotx-alpine`)

Examples of docker image usages are in https://github.com/Knotx/knotx-example-project/docker

#### Pushing Docker image to Docker Hub

The images can be pushed to Docker Hub. Before, be sure you are in the _knotx_ organisation on docker hub (https://hub.docker.com/u/knotx/). Then, add your credentials into `~/.m2/settings.xml`:

```
<server>
  <id>docker-hub</id>
  <username>username</username>
  <password>password</password>
  <configuration>
    <email>email</email>
  </configuration>
</server>
```

Once done, in the `knotx-docker` project just launch:

```
mvn docker:push
```

**WARNING**: This is going to take a while.....

[travis]:https://travis-ci.org/Knotx/knotx-stack
[travis img]:https://travis-ci.org/Knotx/knotx-stack.svg?branch=master

[license]:https://github.com/Cognifide/knotx/blob/master/LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202.0-blue.svg
