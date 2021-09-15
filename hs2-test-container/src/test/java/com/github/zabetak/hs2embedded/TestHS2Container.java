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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TestHS2Container {

  @Rule
  public HS2Container hs2 = new HS2Container();

  @Test
  public void testSimpleDDL() throws Exception {
    try (Connection c = DriverManager.getConnection(hs2.getJdbcURL())) {
      try (PreparedStatement ps = c.prepareStatement(
              "CREATE TABLE test1 (uid VARCHAR(64), link STRING, source STRING)")) {
        ps.executeUpdate();
      }
      try (PreparedStatement ps = c.prepareStatement("DESCRIBE FORMATTED test1")) {
        try (ResultSet rs = ps.executeQuery()) {
          String tableLocation = "";
          while (rs.next()) {
            String property = rs.getString(1);
            String value = rs.getString(2);
            if (property.contains("Location")) {
              tableLocation = value;
            }
          }
          Assert.assertThat(tableLocation, CoreMatchers.containsString("warehouse/test1"));
        }
      }
    }
  }

  @Test
  public void testSimpleCreateInsertSelectStatements() throws Exception {
    try (Connection c = DriverManager.getConnection(hs2.getJdbcURL())) {
      try (PreparedStatement ps = c.prepareStatement(
              "CREATE TABLE test1 (uid VARCHAR(64), link STRING, source STRING)")) {
        ps.executeUpdate();
      }
      List<String> expectedUids = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        final String uid = "A" + i;
        final String link = "L" + i;
        final String source = "S" + i;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO test1 VALUES ('" + uid + "', '" + link + "', '" + source + "')")) {
          ps.executeUpdate();
        }
        expectedUids.add(uid);
      }

      try (PreparedStatement ps = c.prepareStatement("SELECT uid FROM test1 ORDER BY uid ASC")) {
        try (ResultSet rs = ps.executeQuery()) {
          List<String> actualUids = new ArrayList<>();
          while (rs.next()) {
            actualUids.add(rs.getString(1));
          }
          Assert.assertEquals(expectedUids, actualUids);
        }
      }
    }
  }

  @Test
  public void testCreateTransactionalTableAndMaterializedViewOverIt() throws Exception {
    try (Connection c = DriverManager.getConnection(hs2.getJdbcURL())) {
      try (PreparedStatement ps = c.prepareStatement(
              "CREATE TABLE test2 (uid VARCHAR(64), link STRING, source STRING) "
                      + "stored as ORC TBLPROPERTIES ('transactional'='true')")) {
        ps.executeUpdate();
      }
      List<String> expectedUids = new ArrayList<>();
      for (int i = 0; i < 5; i++) {
        final String uid = "A" + i;
        final String link = "L" + i;
        final String source = "S" + i;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO test2 VALUES ('" + uid + "', '" + link + "', '" + source + "')")) {
          ps.executeUpdate();
        }
        expectedUids.add(uid);
      }

      try (PreparedStatement ps = c.prepareStatement(
              "CREATE MATERIALIZED VIEW mat_view AS SELECT * FROM test2")) {
        ps.executeUpdate();
      }

      try (PreparedStatement psTable = c.prepareStatement("SELECT uid FROM test2 ORDER BY uid ASC");
           PreparedStatement psView = c.prepareStatement("SELECT uid FROM mat_view ORDER BY uid ASC")) {
        try (ResultSet rsTable = psTable.executeQuery();
             ResultSet rsView = psView.executeQuery()) {
          List<String> actualTableUids = new ArrayList<>();
          List<String> actualViewUids = new ArrayList<>();
          while (rsTable.next()) {
            actualTableUids.add(rsTable.getString(1));
          }
          while (rsView.next()) {
            actualViewUids.add(rsView.getString(1));
          }
          Assert.assertEquals(expectedUids, actualTableUids);
          Assert.assertEquals(expectedUids, actualViewUids);
        }
      }
    }
  }
}
