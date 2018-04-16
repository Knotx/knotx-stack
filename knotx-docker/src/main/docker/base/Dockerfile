# A base Dockerfile for Knot.x

FROM openjdk:8u151-jre

MAINTAINER Marcin Czeczko <marcin.czeczko@gmail.com>

COPY ./ /usr/local/
RUN chmod +x /usr/local/knotx/bin/knotx

# Set path
ENV KNOTX_HOME /usr/local/knotx
ENV PATH $KNOTX_HOME/bin:$PATH
