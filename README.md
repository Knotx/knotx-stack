[![][travis img]][travis]
[![][license img]][license]

Knotx stack builder
========

## Knot.x Stack

The Knot.x stack is Knot.x + Vert.x + common dependencies.
That's the way you can obtain full instance of running Knot.x with all dependencies.

Structure of the stack is following
```
├── bin
│   └── knotx                     // shell script used to resolve and run knotx instance
├── conf                          // contains application and logger configuration files
│   ├── application.conf          // defines all modules that Knot.x instance is running, provides configuration for Knot.x Core and global variables for other config files
│   ├── bootstrap.json            // config retriever options, defines application configuration stores (e.g. points to `application.conf` - the main configuration)
│   ├── default-cluster.xml       // basic configuration of Knot.x instance cluster
│   ├── includes                  // additional modules configuration which are included in `application.conf`
│   │   ├── actionKnot.conf
│   │   ├── hbsKnot.conf
│   │   ├── httpRepo.conf
│   │   ├── server.conf
│   │   ├── serviceAdapter.conf
│   │   └── serviceKnot.conf
│   └── logback.xml          // logger configuration
├── knotx-stack.json         // stack descriptor, defines instance libraries and dependencies
├── lib                      // contains instance libraries and dependencies, instance classpath
│   ├── list of project dependency libraries
│   ├── ...
```

### Building
Simply run `mvn clean package` to build your version of Knot.x stack or simply use one from the
[latest release](https://github.com/Knotx/knotx-stack/releases/latest). Knot.x stack artifact is a ZIP file
with the structure described above. To start playing with Knot.x stack unzip the archive.

### Running Stack
From the root stack folder, execute:
```cmd
bin/knotx run-knotx
```
to run the instance.

### Resolving dependencies
To resolve all the dependencies defined in the `knotx-stack.json` execute from the root stack folder:
```cmd
bin/knotx resolve
```

### Debug resolver
Enable TRACE level logging on your logback.xml
```xml
<logger name="knotx-stack-resolver" level="TRACE"/>
```

### Working with snapshots
`-Dknotx.maven.snapshotPolicy=0`


## Docker
To build docker images, you need docker 1.3+ and run `mvn clean install`
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
