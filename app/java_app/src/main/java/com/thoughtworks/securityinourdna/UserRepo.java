package com.thoughtworks.securityinourdna;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    private final Connection connection;

    public UserRepo(Connection connection) {
        this.connection = connection;
    }

    public void addName(String vendorName, String password) throws Exception {
        final String query = "insert into users values ('" + vendorName + "', '" + password + "')";
        connection.createStatement().execute(query);
    }

    public List<String> allVendors() throws SQLException {
        List<String> vendors = new ArrayList<>();
        final String query = "select vendor_name from users";
        final ResultSet resultSet = connection.createStatement().executeQuery(query);
        while(resultSet.next()){
            vendors.add(resultSet.getString("vendor_name"));
        }
        return vendors;
    }

    public LoginResult login(String vendorName, String password) throws SQLException {
        final String query = "select * from users where vendor_name = '" + vendorName + "' and password = '" + password + "'";

        final ResultSet resultSet = connection.createStatement().executeQuery(query);

        if (resultSet.next()) {
            return LoginResult.success(resultSet.getString(1));
        } else {
            return LoginResult.failed();
        }
    }

    public static class LoginResult {

        public final Boolean success;
        public final String vendorName;

        private LoginResult(Boolean success, String vendorName) {
            this.success = success;
            this.vendorName = vendorName;
        }

        public static LoginResult success(String vendorName) {
            return new LoginResult(true, vendorName);
        }

        public static LoginResult failed() {
            return new LoginResult(false, null);
        }
    }
}
