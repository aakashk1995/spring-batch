package com.infybuzz.faker;

import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InsertRecords {

   static String jdbcURL = "jdbc:mysql://cms-dev:3306/mydb"; // Replace with your database URL
   static   String dbUser = "root";                        // Replace with your username
    static String dbPassword = "root";

    public static void main(String[] args) {
        Connection connection = null;

        // Use Faker to generate fake data
        Faker faker = new Faker();

        try{
            connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
            connection.setAutoCommit(false); // Enable manual transaction control

            String insertSQL = "INSERT INTO student (first_name, last_name, email) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

            List<String> emails = new ArrayList<>(); // To ensure unique emails
            int batchSize = 1000;
            int totalRecords = 100000;

            for (int i = 1; i <= totalRecords; i++) {
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                String email;

                // Ensure unique email generation
                do {
                    email = faker.internet().emailAddress();
                } while (emails.contains(email));
                emails.add(email);

                // Add to batch
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, email);
                preparedStatement.addBatch();

                // Execute batch every 1000 records
                if (i % batchSize == 0) {
                    preparedStatement.executeBatch();
                    connection.commit();
                    System.out.println(i + " records inserted...");
                }
            }

            // Insert remaining records
            preparedStatement.executeBatch();
            connection.commit();
            System.out.println("All records inserted successfully!");
        }catch (Exception ex){
            ex.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("Transaction rolled back due to error.");
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        }

    }
}
