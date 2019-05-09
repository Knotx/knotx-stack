# A base Dockerfile for Knot.x

FROM openjdk:8-jre
LABEL maintainer="Knot.x Project"

COPY ./ /usr/local/
RUN chmod +x /usr/local/knotx/bin/knotx

# Set path
ENV KNOTX_HOME /usr/local/knotx
ENV PATH $KNOTX_HOME/bin:$PATH
