[![][travis img]][travis]
[![][license img]][license]

# Knot.x Stack
Knot.x Stack is the Knot.x distribuition cotaining all dependencies, configuration files and execution scripts.


## Distribution structure

Structure of the stack is following
```
├── bin
│   └── knotx                     // shell script used to resolve and run knotx instance
├── conf                          // contains application and logger configuration files
│   ├── application.conf          // defines / includes all modules that Knot.x instance is running
│   ├── bootstrap.json            // config retriever options, defines application configuration stores (e.g. points to `application.conf` - the main configuration)
│   ├── default-cluster.xml       // basic configuration of Knot.x instance cluster
│   ├── openapi.yaml              // Open API 3.0 configuration that is loaded via Knot.x HTTP Server
│   ├── server.conf               // Knot.x HTTP server configuration which is included in `application.conf`
│   ├── routes                    // server routes configurations 
│   │   ├── operation-get.conf
│   │   └── handlers              // handlers used in API operations definitions
|   │   │   ├── knotEngineHandler.conf
|   │   │   └── httpRepoConnectorHandler.conf
│   ├── knots                     // Knot modules configurations which are included in `application.conf`
│   │   ├── dataBridgeStack.conf
│   │   ├── dataBridgeKnot.conf
│   │   ├── dataBridgeSourceHttp.conf
│   │   ├── templateEngineStack.conf
│   │   └── templateEngineKnot.conf
│   └── logback.xml          // logger configuration
├── knotx-stack.json         // stack descriptor, defines instance libraries and dependencies
├── lib                      // contains instance libraries and dependencies, instance classpath
│   ├── list of project dependency libraries
│   ├── ...
```

## Building
Simply run `gradlew build` to build your version of Knot.x stack or simply use one from the
[latest release](https://github.com/Knotx/knotx-stack/releases/latest). Knot.x stack artifact is a ZIP file
with the structure described above. To start playing with Knot.x stack unzip the archive.

## Running Stack
From the root stack folder, execute:
```cmd
bin/knotx run-knotx
```
to run the instance.


[travis]:https://travis-ci.org/Knotx/knotx-stack
[travis img]:https://travis-ci.org/Knotx/knotx-stack.svg?branch=master

[license]:https://github.com/Cognifide/knotx/blob/master/LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202.0-blue.svg
