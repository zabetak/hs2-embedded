<!--
   Copyright 2020 Stamatis Zampetakis

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
# Hiveserver2 Embedded

A project for building and using a docker image of [Hiveserver2](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Overview),
pre-configured to run with [Tez](https://tez.apache.org/) in local mode as the underlying execution engine and in-memory
[Derby](https://db.apache.org/derby/) as the metastore.

Use `mvn install` to build the project and create the docker image for HS2.

You can use the docker image by managing
containers explicitly via the docker CLI or by exploiting the dependency on https://www.testcontainers.org/.

Create HS2 container using Docker CLI:

    docker run -p "10000:10000" -d com.github.zabetak/hs2-embedded:1.0.4.7.2.3.0-SNAPSHOT

(Optional) Use the `schema-loader` utility to create some tables and start playing around:

    docker exec CONTAINER_NAME schema-loader tpcds

Create HS2 container using testcontainers:

    public class TestHS2Container {

      @Rule
      public HS2Container hs2 = new HS2Container();

      @Test
      public void testSimpleDDL() throws Exception {
        try (Connection c = DriverManager.getConnection(hs2.getJdbcURL())) {
          try (PreparedStatement ps = c
              .prepareStatement("CREATE TABLE test1 (uid VARCHAR(64), link STRING, source STRING)")) {
            ps.executeUpdate();
          }
        }
      }
    }

## Release

This section contains information for developers who would like to
release the project. 

The manual steps to perform a release are shown below:

1. Pick a concrete version for the release (freeze -SNAPSHOT) and commit the changes.
```
mvn versions:set -DnewVersion=1.0.4.7.2.3.0-220
```
2. Verify that build using the release profile finishes successfully.
```
mvn package -Prelease
```
3. Upload the artifacts to the nexus staging repository
```
mvn deploy -Prelease
```
4. Visit the nexus staging repository and verify the content (sha, asc, jar, sources, etc.).
5. Close and then release the staging repository.
6. Create a tag corresponding to the newly release version in the git repository.
```
git tag 1.0.4.7.2.3.0-220
```
7. Push tag to the remote repository
```
git push origin 1.0.4.7.2.3.0-220
```
8. Prepare for next depelopment iteration
8a. Change version in pom files
```
mvn versions:set -DnewVersion=1.0.5.7.2.3.0-SNAPSHOT
```
8b. Update release instructions in README.md to reflect version changes
8c. Commit
9. Push master to the remote repository
```
git push origin master
```

More details about the release steps can be found
[here](https://central.sonatype.org/publish/publish-maven/).
