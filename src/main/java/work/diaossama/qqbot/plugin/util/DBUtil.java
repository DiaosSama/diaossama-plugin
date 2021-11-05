package work.diaossama.qqbot.plugin.util;

import java.sql.*;

public class DBUtil {
    private Connection conn;
    private static final String DATABASE_ADDR = "jdbc:sqlite:data/DiaosSama-Plugin/database.db";

    public static void test() {
        System.out.println(System.getProperty("user.dir"));
    }

    public DBUtil() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(DATABASE_ADDR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet query(String sql) {
        try {
            Statement stmt = this.conn.createStatement();
            return stmt.executeQuery(sql);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean insert(String sql) {
        try {
            Statement stmt = this.conn.createStatement();
            int res = stmt.executeUpdate(sql);
            return res > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(String sql) {
        try {
            Statement stmt = this.conn.createStatement();
            int res = stmt.executeUpdate(sql);
            return res > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String sql) {
        try {
            Statement stmt = this.conn.createStatement();
            int res = stmt.executeUpdate(sql);
            return res > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            this.conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 首次使用插件初始化数据库
    private void initDatabase() {
    }

}
