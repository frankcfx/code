package myspark.hbase;

import java.sql.*;

/**
 * Created by hadoop on 18-2-21.
 */
public class PhoenixJDBC {


    public static void main(String[] args) {
        //getConn();
        PhoenixJDBC phoenix = new PhoenixJDBC();
        phoenix.query();
        //phoenix.createTable();
        //phoenix.upsert();
    }


    /**
     * 建表
     */
    public void createTable(){
        try {
            Connection conn = getConn();
            Statement sm = conn.createStatement();
            sm.executeUpdate("create table test_phoenix(orderid VARCHAR not null PRIMARY key, name VARCHAR)");
            sm.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 插入/更新
     */
    public void upsert(){
        Connection conn = getConn();
        ResultSet rs = null;
        String upsertSQL = "upsert into test_phoenix(orderid, name) values(?, ?)";
        try {
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(upsertSQL);
            for(int i = 0; i<1000; i++){
                String orderid=  String.format("%05d", i);
                String name = "good" + Math.random();
                pstmt.setString(1, orderid);
                pstmt.setString(2, name);
                pstmt.addBatch();
                // 每100条记录commit一次
                if ((i+1) % 100 == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    Thread.sleep(2000);
                }
            }
            pstmt.executeBatch();
            conn.commit();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    /**
     * 查询phoenix数据
     */
    public void query(){

        Connection conn = getConn();
        ResultSet rs = null;

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement("select symbol, company from STOCK_SYMBOL");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("symbol:"+rs.getString(1) + ", company:" + rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                rs.close();
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 获取jdbc连接
     * @return
     */
    public Connection getConn() {
        Connection conn = null;

        try {
            String driver = "org.apache.phoenix.queryserver.client.Driver";
            String jdbcUrl =  "jdbc:phoenix:thin:url=http://hbase-master1:8765;serialization=PROTOBUF;";

            Class.forName(driver);
            conn = DriverManager.getConnection(jdbcUrl, "", "");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }
}