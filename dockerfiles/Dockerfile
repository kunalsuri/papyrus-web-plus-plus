#############################################################################
#  Copyright (c) 2025, 2026 CEA LIST.
#  
#  This program and the accompanying materials
#  are made available under the terms of the Eclipse Public License 2.0
#  which accompanies this distribution, and is available at
#  https://www.eclipse.org/legal/epl-2.0/
# 
#  SPDX-License-Identifier: EPL-2.0
#  
#  Contributors:
#   Marcos Didonet Del Fabro (CEA LIST) - Initial implementation
#
#############################################################################

# Build instructions. Under the root folder
# docker build --build-arg USERNAME=${GIT_USERNAME} PASSWORD=${GIT_AUTH_TOKEN} -t papyrusweb ..
# Launch instructions. Note that it is necessary to have a running postgres image or local database
# docker run --name=papyrusweb-container -p 8080:8080 papyrusweb:latest

# Image for full compilation

FROM node:22.16 AS papyrusweb-front-build

ADD . /build
WORKDIR /build/

ARG USERNAME
ARG PASSWORD

# Compile the frontend in one layer

RUN echo '//npm.pkg.github.com/:_authToken=${PASSWORD}' >> /build/frontend/.npmrc &&\
  cd frontend && npm install && npm ci && npm run build
RUN mkdir -p backend/papyrus-web-frontend/src/main/resources/static &&\
  cp -R frontend/papyrus-web/dist/* backend/papyrus-web-frontend/src/main/resources/static

# Update packages and compile the backend in another layer

FROM maven:3.9.9-eclipse-temurin-21 AS papyrusweb-back-build

ARG USERNAME
ARG PASSWORD
ENV USERNAME=${USERNAME}
ENV PASSWORD=${PASSWORD}

COPY --from=papyrusweb-front-build /build /build
WORKDIR /build

RUN mvn clean verify -DskipTests -Dcheckstyle.skip -f backend/pom.xml --settings settings.xml

RUN export WEBAPP_VERSION=$(grep version frontend/package.json | awk -F '\"' '{print $4}') && \
    cp backend/papyrus-web/target/papyrus-web-${WEBAPP_VERSION}.jar backend/papyrus-web/target/papyrus-web.jar

# Image to run Papyrus Web

FROM eclipse-temurin:21-jre AS papyrus-web

COPY --from=papyrusweb-back-build /build/backend/papyrus-web/target/papyrus-web.jar /papyrus-web.jar

RUN ls -la /

EXPOSE 8080

ENTRYPOINT ["java","-jar","/papyrus-web.jar"]
