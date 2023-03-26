package org.artzhelt.jdbcdemo;


import java.sql.*;


public class JDBCDemo {

    private Connection connection;
    private Statement statement;

    public JDBCDemo(Connection connection, Statement statement) {
        this.connection = connection;
        this.statement = statement;
    }

    public static void main(String[] args ) {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/test");
             Statement statement = connection.createStatement())
        {
            JDBCDemo jdbcDemo = new JDBCDemo(connection, statement);
            jdbcDemo.createSchema();
            jdbcDemo.insertData();
            jdbcDemo.showData();

            jdbcDemo.updatePositionById(1, "lead developer");
            jdbcDemo.showData();

            jdbcDemo.updatePositionAndSalaryById(1, "junior developer", 700);
            jdbcDemo.showData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createSchema() throws SQLException {
        String dropSchemaSql = "DROP TABLE IF EXISTS employees";
        statement.execute(dropSchemaSql);
        String schemaSql = """
                                CREATE TABLE IF NOT EXISTS employees
                                (emp_id int PRIMARY KEY AUTO_INCREMENT, name varchar(30),
                                position varchar(30), salary double)""";
        statement.execute(schemaSql);
    }

    public void insertData() throws SQLException {
        String dataSql = """
                             INSERT INTO employees(name, position, salary)
                             VALUES('john', 'developer', 2000)""";
        statement.executeUpdate(dataSql);
    }

    public void showData() throws SQLException {
        String querySql = "SELECT * FROM employees";
        try (ResultSet rs = statement.executeQuery(querySql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                String position = rs.getString("position");
                int salary = rs.getInt("salary");
                System.out.format("name: %s, position: %s, salary: %d%n", name, position, salary);
            }
        }
    }

    public int updatePositionById(int id, String position) throws SQLException {
        String updateSql = "UPDATE employees SET position=?1 WHERE emp_id=?2";
        try (PreparedStatement prStmt = connection.prepareStatement(updateSql)){
            prStmt.setString(1, position);
            prStmt.setInt(2, id);
            return prStmt.executeUpdate();
        }
    }

    public int updateSalaryById(int id, int salary) throws SQLException {
        String updateSql = "UPDATE employees SET salary=?1 WHERE emp_id=?2";
        try (PreparedStatement prStmt = connection.prepareStatement(updateSql)) {
            prStmt.setInt(1, salary);
            prStmt.setInt(2, id);
            return prStmt.executeUpdate();
        }
    }

    public void updatePositionAndSalaryById(int id, String position, int salary) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            updatePositionById(id, position);
            throwTestException();
            updateSalaryById(id, salary);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            e.printStackTrace();
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private void throwTestException() {
        throw new RuntimeException("test exception");
    }
}
