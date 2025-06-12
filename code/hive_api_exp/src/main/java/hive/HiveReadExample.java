package hive_api_exp.src.main.java.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class HiveReadExample {
    // JDBC URL, username, and password
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String jdbcUrl = "jdbc:hive2://m1:10000/default"; // 根据实际Host和Port调整
    private static String user = "root";  // 根据Hive配置修改
    private static String password = "1111121";  // 根据Hive配置修改
    public static void main(String[] args) {
        try {
            // 1. 加载 Hive JDBC 驱动
            Class.forName(driverName);
            // 2. 创建连接
            Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
            Statement stmt = conn.createStatement();
            // 3. 执行查询
            String sql = "SELECT id, name, age FROM part1";
            ResultSet res = stmt.executeQuery(sql);
            // 4. 打印结果
            System.out.println("ID\tName\tAge");
            while (res.next()) {
                int id = res.getInt("id");
                String name = res.getString("name");
                int age = res.getInt("age");
                System.out.println(id + "\t" + name + "\t" + age);
            }
            // 5. 关闭连接
            res.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
