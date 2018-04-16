# Knotx.x Docker Images

This project builds the docker images for the Knot.x

## Build the images

You need to have installed **docker** on your machine.

To build the docker images, just launch:

`mvn clean install`

## Knotx.x docker base Image

The built image contains the `knotx` command in the system path.
 
### Using the base image

The image is intended to be used by extension using the Docker `FROM` directive. Here is an example:

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



```Dockerfile
FROM knotx/knotx

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

`docker build -t mycompany/my-knotx-exec .`

And run your verticle:

```
docker run -i -t -p 8080:8080 \
    -v $PWD:/verticles mycompany/my-knotx-exec \
    run run-knotx
```

### Launching the base image

The resulting image is not made to be launched directly (as it contains only knot.x and no applications). If you 
still want to launch it uses:
 
`docker run -i -t knotx/knotx`

The knotx.x files are located in ` /usr/local/knotx/`.

You can access the `knotx` command directly using:

`docker run -i -t knotx/knotx knotx`

## Knot.x executable image from Docker

A Docker image providing the `knotx` command

### TBD - launching

Notice the export CLASSPATH=…​; part in the CMD instruction. It builds the value of the CLASSPATH variable from the content of the $VERTICLE_HOME directory. This tricks is useful to compute large and dynamic classpath.

-----

Just launch:

`docker run -i -t knotx/knotx-exec`

Append the `knotx` command parameter you need.

for instance:

```
> docker run -i -t knotx/knotx-exec -version
3.0.0-SNAPSHOT
```

If you want to run a verticle:

```
docker run -i -t -p 8080:8080 \
    -v $PWD:/verticles knotx/knotx-exec \
    run io.vertx.sample.RandomGeneratorVerticle \
    -cp /verticles/MY_VERTICLE.jar
```

This command mount the current directory into `/verticles` and then launch the `knotx run` command. Notice the `-cp`
parameter reusing the `/verticles` directory.
