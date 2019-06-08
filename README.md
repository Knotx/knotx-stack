[![][travis img]][travis]
[![][license img]][license]

# Knot.x Stack
Knot.x Stack is a Knot.x distribution containing all dependencies, configuration files and running scripts. 
It does not require any external dependencies so it is used to build [Knot.x Docker](https://github.com/Knotx/knotx-docker) image.

## Distribution structure

Structure of the stack is following
```
├── bin
|   ├── knotx                     // shell script used to run knotx instance
│   └── knotx.bat                 // Windows script used to run knotx instance                      
├── conf                          // contains application and logger configuration files
│   ├── application.conf          // defines / includes all modules that Knot.x instance is running
│   ├── bootstrap.json            // config retriever options, defines application configuration stores (e.g. points to `application.conf` - the main configuration)
│   ├── openapi.yaml              // Open API 3.0 configuration that is loaded via Knot.x HTTP Server
│   ├── server.conf               // Knot.x HTTP server configuration which is included in `application.conf`
│   ├── routes                    // server routes configurations 
│   │   ├── operations.conf       // defines handlers per Open API operation ids
│   │   └── handlers              // handlers configurations used in `operations.conf`
|   │   │   ├── fragmentsHandler.conf
|   │   │   └── httpRepoConnectorHandler.conf
│   ├── knots                     // Knot modules configurations which are included in `application.conf`
│   │   ├── templateEngineStack.conf
│   │   └── templateEngineKnot.conf
│   └── logback.xml          // logger configuration
├── lib                      // contains instance libraries and dependencies, instance classpath
│   ├── list of project dependency libraries
│   ├── ...
```

## Building
Simply run `gradlew build` to build your version of Knot.x stack or simply use one from the
[latest release](https://github.com/Knotx/knotx-stack/releases/latest). Knot.x stack artifact is a 
ZIP file (see the `build/distributions` folder) with the structure described above.

## Knot.x developers support
To develop Knot.x stack and any of its dependencies locally do following steps:
1. clone all Knot.x stack repositories into one directory (this will be a `{KNOTX_ROOT}`):
```
https://github.com/Knotx/knotx-dependencies
https://github.com/Knotx/knotx-commons
https://github.com/Knotx/knotx-junit5
https://github.com/Knotx/knotx-launcher
https://github.com/Knotx/knotx-fragment-api
https://github.com/Knotx/knotx-server-http
https://github.com/Knotx/knotx-repository-connector
https://github.com/Knotx/knotx-fragments-handler
https://github.com/Knotx/knotx-data-bridge
https://github.com/Knotx/knotx-template-engine
https://github.com/Knotx/knotx-stack
```
2. create `.composite-enabled` file in the `Knotx/knotx-stack`
```
touch knotx-stack/.composite-enabled
```
3. import `knotx-stack` project in IntelliJ as a Gradle project.

You should see after a while all the Knot.x stack projects in single Idea window. Congratulations! 
We now wait for your contribution :)

> Note: You may use [Knot.x aggregator](https://github.com/Knotx/knotx-aggregator#development-process)
that will do (1) and (2) steps for you.

## Running Stack
To start playing with Knot.x stack unzip the archive.
From the `knotx-stack` folder, execute:
```cmd
bin/knotx run-knotx
```
or
```cmd
bin/knotx.bat run-knotx
```
to run the instance.


[travis]:https://travis-ci.org/Knotx/knotx-stack
[travis img]:https://travis-ci.org/Knotx/knotx-stack.svg?branch=master

[license]:https://github.com/Cognifide/knotx/blob/master/LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202.0-blue.svg
