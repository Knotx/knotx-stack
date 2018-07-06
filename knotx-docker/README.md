# Knot.x Docker Images

This project builds the docker images for the Knot.x

## Build the images

You need to have installed **docker** on your machine.

To build the docker images locally, just launch:

`mvn clean install`

## Knot.x docker base Image

The built image contains the `knotx` command in the system path.
 
### Using the base image

The image is intended to be used by extension using the Docker `FROM` directive.

#### Setting up the stack
The knotx/knotx-exec image provides the default "full" Vert.x stack. You may want to customize this stack and create your own exec image. First, create a vertx-stack.json file:

```json
{
  "variables": {
    "vertx.version": "3.3.3"
  },
  "dependencies": [
    {
      "groupId": "io.vertx",
      "artifactId": "vertx-web",
      "version": "${vertx.version}",
      "included": true
    },
    {
      "groupId": "io.vertx",
      "artifactId": "vertx-lang-js",
      "version": "${vertx.version}",
      "included": true
    }
  ]
}
```
You can list any dependency you need, not just the Vert.x artifacts (refer to the Stack Manager documentation for details).

First line of your Dockerfile should say what docker image you're going to start with. Use `knotx/knotx:<version>` where `<version>` is the version of Knot.x the immage is going to run.
In this example we're using `1.3.0`, for other version simply look at `https://hub.docker.com/r/knotx/knotx/tags/` or `https://hub.docker.com/r/knotx/knotx-alpine/tags/` if you want to use alpine image.

**NOTE: Avoid using `latest` tag, as it point always to latest build image, that in most cases if SNAPSHOT images**

```Dockerfile
FROM knotx/knotx:1.3.0

# Set the JVM Options
ENV JAVA_OPTS "-Dfoo=bar"

# Set vertx options
ENV VERTX_OPTS "-Dvertx.options.eventLoopPoolSize=26 -Dvertx.options.deployment.worker=true"

ENV APPLICATION_HOME=/usr/local/custom-app

### Customize logging, if needed
COPY ./logback.xml ${KNOTX_HOME}/conf/logback.xml                 

## Customize Clustering if needed
COPY ./my-cluster.xml ${KNOTX_HOME}/conf/my-cluster.xml
ENV CLUSTER_CONFIG, ${KNOTX_HOME}/conf/my-cluster.xml

## In order to supply your custom configuration of Knot.x modules you'd need to supply
## your custom config folder consisting of bootstrap.json and corresponding .conf files
ADD ./config ${KNOTX_HOME}/my-config
ENV KNOTX_MAIN_CONFIG ${KNOTX_HOME}/my-config/bootstrap.json

## Add new dependencies to the Knot.x stack, a dependencies to your custom code
COPY knotx-stack.json ${KNOTX_HOME}/knotx-stack.json
RUN knotx resolve -conf ${KNOTX_MAIN_CONFIG} && rm -rf ${HOME}/.m2 

###
# The rest of the file should be fine.
###

# We use the "sh -c" to turn around https://github.com/docker/docker/issues/5509 - variable not expanded
ENTRYPOINT ["sh", "-c"]
CMD ["knotx", "run-knotx", "-conf $KNOTX_MAIN_CONFIG"]
```

You should know be able to build your custom executable image:

`docker build -t mycompany/my-knotx .`

And run your instance:

```
docker run -i -t -p 8092:8092 mycompany/my-knotx
```

### Launching the base image

The resulting image is not made to be launched directly (as it contains only core knot.x and no default configurations). If you 
still want to launch it and see, whats in the image, just do:
 
`docker run -i -t knotx/knotx:1.3.0`

The knotx.x files are located in ` /usr/local/knotx/`.

You can access the `knotx` command directly using:

`docker run -p8092:8092 knotx/knotx:1.3.0 knotx`

Or simply try to run a knotx in a container

`docker run -p8092:8092 knotx/knotx:1.3.0 knotx run-knotx`
