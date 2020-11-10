# Copyright 2020 Stamatis Zampetakis
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM maven:3.6-jdk-8 AS build
COPY hs2-launcher/src /usr/src/app/hs2-launcher/src
COPY hs2-launcher/pom.xml /usr/src/app/hs2-launcher/pom.xml
COPY pom.xml /usr/src/app/
WORKDIR /usr/src/app/hs2-launcher
RUN mvn install -P '!dockerlocal'

FROM openjdk:8-jre-slim

ARG VERSION

COPY --from=build /usr/src/app/hs2-launcher/target/lib /opt/hive/lib
COPY --from=build /usr/src/app/hs2-launcher/target/hs2-launcher-${VERSION}.jar /opt/hive/hs2-launcher.jar

WORKDIR /opt/hive

EXPOSE 10000

CMD java -jar hs2-launcher.jar