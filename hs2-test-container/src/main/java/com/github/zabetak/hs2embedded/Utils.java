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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

class Utils {

  static String getDockerImageName() {
    try (InputStream input = Utils.class.getClassLoader().getResourceAsStream("container.properties")) {
      Properties prop = new Properties();
      prop.load(input);
      return Objects.requireNonNull(prop.getProperty("docker.image.name"));
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
