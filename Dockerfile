FROM java:8-jre-alpine

RUN apk update \
    && apk add unzip \
    && apk add wget \
    && apk add ca-certificates \
    && apk add bash

ENV LANG fr_FR.UTF-8
ENV LANGUAGE fr_FR.UTF-8
ENV LC_ALL fr_FR.UTF-8

RUN  mkdir /conf
WORKDIR /app

# Copy project files and unzip them
COPY target/universal/crm.zip /app/
RUN unzip /app/crm.zip

RUN wget -O /dd-java-agent.jar https://dtdg.co/latest-java-tracer

ENTRYPOINT [ "/app/crm/bin/crm",  "-J-javaagent:/dd-java-agent.jar"]
