package com.github.zabetak.hs2embedded;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class HS2Loader {
  private static final String DEFAULT_URL = "jdbc:hive2://localhost:10000";

  public static void main(String[] args) throws ParseException, SQLException, IOException, InterruptedException {
    Options options = new Options();
    options.addRequiredOption("s", "schema", true, "the schema to load: TPCDS");
    options.addOption("u", "url", true, "the JDBC url to connect to (default is ");
    options.addOption("t", "tries", true, "max number of tries to connect to JDBC (default is 5)");
    CommandLineParser parser = new DefaultParser();
    CommandLine cl = parser.parse(options, args);
    final int maxTries = cl.hasOption("t") ? Integer.parseInt(cl.getOptionValue("t")) : 5;
    final String url = cl.hasOption("u") ? cl.getOptionValue("u") : DEFAULT_URL;
    int tries = 0;
    boolean connect = false;

    while (!connect && tries < maxTries) {
      try (Connection ignored = DriverManager.getConnection(url)) {
        connect = true;
      } catch (SQLException e) {
        tries++;
        Thread.sleep(5000);
      }
    }
    if (!connect) {
      throw new IllegalAccessError("Couldn't connect to the HS2 after " + maxTries + " tries");
    }
    final String schema = cl.getOptionValue("s").toLowerCase();
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(schema + "/tables.sql")) {
      if (is == null) {
        throw new IllegalArgumentException("Schema " + schema + " does not exist");
      }
      try (Scanner sc = new Scanner(is)) {
        sc.useDelimiter(";");
        try (Connection c = DriverManager.getConnection(url)) {
          while (sc.hasNext()) {
            String stmt = sc.next().trim();
            if (!stmt.isEmpty()) {
              try (PreparedStatement ps = c.prepareStatement(stmt)) {
                ps.executeUpdate();
              }
            }
          }
        }
      }
    }
  }
}
