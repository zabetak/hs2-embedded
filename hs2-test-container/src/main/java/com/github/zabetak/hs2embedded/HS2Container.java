/**
 * Copyright 2020 Stamatis Zampetakis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zabetak.hs2embedded;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class HS2Container extends GenericContainer<HS2Container> {

  public HS2Container(){
    super(DockerImageName.parse(Utils.getDockerImageName()));
    withExposedPorts(10000);
  }

 public String getJdbcURL() {
   return "jdbc:hive2://" + this.getHost() + ":" + this.getMappedPort(10000);
  }

}
