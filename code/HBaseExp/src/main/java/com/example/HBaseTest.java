package com.example;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * HBase操作示例类
 * 演示了HBase表的创建、删除以及数据的插入和删除操作
 */
public class HBaseTest {
    // 定义配置文件的静态变量，用于连接HBase集群
    static Configuration conf = HBaseConfiguration.create();


    /**
     * 创建HBase表
     * @param tableName 表名
     * @param columnFamilies 列族名数组
     */
    public static void createTable(String tableName, String[] columnFamilies) throws Exception {
        // 使用try-with-resources确保资源正确关闭
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Admin admin = connection.getAdmin()) { // 获取HBaseAdmin对象，用于管理表

            // 检查表是否已存在
            if (admin.tableExists(TableName.valueOf(tableName))) {
                System.out.println(tableName + " exists!");
            } else {
                // 使用TableDescriptorBuilder构建表描述符
                TableDescriptorBuilder tableBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName));

                // 添加每个列族到表描述符
                for (String family : columnFamilies) {
                    // 使用ColumnFamilyDescriptorBuilder构建列族描述符
                    ColumnFamilyDescriptor cf = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(family)).build();
                    // 将列族添加到表描述符中
                    tableBuilder.setColumnFamily(cf);
                }

                // 创建表描述符
                TableDescriptor table = tableBuilder.build();

                // 调用createTable创建表
                admin.createTable(table);
                System.out.println(tableName + " create successfully with column families: " +
                        String.join(", ", columnFamilies));
            }
        }
    }

    /**
     * 删除HBase表
     * @parableName 要删除的表名
     */
    public static void deleteTable(String tableName) throws IOException {
        // 使用try-with-resources确保资源正确关闭
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Admin admin = connection.getAdmin()) {

            // 检查表是否存在
            if (admin.tableExists(TableName.valueOf(tableName))) {
                System.out.println(tableName + " exists!");
                try {
                    // 必须先禁用表，然后才能删除
                    admin.disableTable(TableName.valueOf(tableName));
                    admin.deleteTable(TableName.valueOf(tableName));
                    System.out.println(tableName + " delete successfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(tableName + " delete failed!");
                }
            } else {
                System.out.println(tableName + " does not exist!");
            }
        }
    }

    /**
     * 向HBase表中插入单条数据
     * @param tableName 表名
     * @param rowKey 行键
     * @param colFamily 列族名
     * @param col 列名
     * @param value 要插入的值
     */
    public static void insertData(String tableName, String rowKey, String colFamily,
                                  String col, String value) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            // 创建Put对象，指定行键
            Put put = new Put(Bytes.toBytes(rowKey));

            // 向Put对象中添加列族、列、值
            put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(value));

            // 执行插入操作
            table.put(put);
            System.out.println("Data inserted successfully to table: " + tableName +
                    ", RowKey: " + rowKey +
                    ", Column: " + colFamily + ":" + col +
                    ", Value: " + value);
        }
    }

    /**
     * 向HBase表中插入多列族数据
     * 该方法允许同时向不同列族插入数据
     *
     * @param tableName 表名
     * @param rowKey 行键
     * @param colFamilies 列族名数组
     * @param cols 列名数组(与列族数组一一对应)
     * @param values 值数组(与列族和列名数组一一对应)
     * @throws IOException 如果插入过程中发生错误
     */
    public static void insertMultiFamilyData(String tableName, String rowKey,
                                             String[] colFamilies, String[] cols,
                                             String[] values) throws IOException {
        // 检查参数是否合法
        if (colFamilies.length != cols.length || cols.length != values.length) {
            System.out.println("列族、列名和值的数量不匹配!");
            return;
        }

        try (Connection connection = ConnectionFactory.createConnection(conf);
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            // 创建Put对象，指定行键
            Put put = new Put(Bytes.toBytes(rowKey));

            // 添加每个列族的数据
            for (int i = 0; i < colFamilies.length; i++) {
                put.addColumn(Bytes.toBytes(colFamilies[i]),
                        Bytes.toBytes(cols[i]),
                        Bytes.toBytes(values[i]));
            }

            // 执行插入操作
            table.put(put);
            System.out.println("Multi-family data inserted successfully to table: " + tableName +
                    ", RowKey: " + rowKey);
        }
    }


    /**
     * 删除HBase表中特定行的特定列数据
     *
     * @param tableName 表名
     * @param rowKey 行键
     * @param colFamily 列族名
     * @param col 要删除的列名
     * @throws IOException 如果删除过程中发生错误
     */
    public static void deleteColumn(String tableName, String rowKey, String colFamily,
                                    String col) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            // 创建Delete对象，指定行键
            Delete delete = new Delete(Bytes.toBytes(rowKey));

            // 指定要删除的列
            delete.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col));

            // 执行删除操作
            table.delete(delete);
            System.out.println("Column " + colFamily + ":" + col + " in row " + rowKey +
                    " deleted successfully from table: " + tableName);
        }
    }

    /**
     * 删除HBase表中的一行数据
     * @param tableName 表名
     * @param rowKey 要删除的行键
     * @throws IOException 如果删除过程中发生错误
     */
    public static void deleteRow(String tableName, String rowKey) throws IOException {
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Table table = connection.getTable(TableName.valueOf(tableName))) {

            // 创建Delete对象，指定要删除的行
            Delete delete = new Delete(Bytes.toBytes(rowKey));

            // 执行删除操作
            table.delete(delete);
            System.out.println("Row " + rowKey + " deleted successfully from table: " + tableName);
        }
    }

    /**
     * 主函数，演示HBase操作 - 学生信息和成绩管理示例
     */
    public static void main(String[] args) {
        // 设置ZooKeeper集群地址，注意修改为实际的服务器hostname
        conf.set("hbase.zookeeper.quorum", "m1,m2,m3");

        String tableName = "student_table";
        // 创建两个列族：StudentInfo和ScoreInfo
        String[] columnFamilies = {"StudentInfo", "ScoreInfo"};

        try {
            // 1. 创建表
            System.out.println("--------- 创建学生信息表 ---------");
            HBaseTest.createTable(tableName, columnFamilies);

            // 2. 插入第一个学生的基本信息（单条插入方式）
            System.out.println("\n--------- 插入学生1基本信息 ---------");
            HBaseTest.insertData(tableName, "1", "StudentInfo", "name", "张三");
            HBaseTest.insertData(tableName, "1", "StudentInfo", "stu_id", "221");
            HBaseTest.insertData(tableName, "1", "ScoreInfo", "Chinese", "90");
            HBaseTest.insertData(tableName, "1", "ScoreInfo", "math", "100");


            // 4. 同时插入第二个学生的多个列族信息（多列族同时插入）
            System.out.println("\n--------- 同时插入学生2的基本信息和成绩 ---------");
            String[] families = {"StudentInfo", "StudentInfo", "ScoreInfo", "ScoreInfo"};
            String[] cols = {"name", "stu_id", "Chinese", "math"};
            String[] vals = {"李四", "222", "88", "70"};
            HBaseTest.insertMultiFamilyData(tableName, "2", families, cols, vals);


            // 6. 修改学生成绩（更新数据）
            System.out.println("\n--------- 修改学生1的数学成绩 ---------");
            HBaseTest.insertData(tableName, "1", "ScoreInfo", "math", "95");

            // 7. 删除特定列（删除学生2的语文成绩）
            System.out.println("\n--------- 删除学生2的语文成绩 ---------");
            HBaseTest.deleteColumn(tableName, "2", "ScoreInfo", "Chinese");

            // 9. 删除整行（删除学生2的所有信息）
            System.out.println("\n--------- 删除学生2的所有信息 ---------");
            HBaseTest.deleteRow(tableName, "2");

            // 10. 删除表
            System.out.println("\n--------- 删除表 ---------");
            HBaseTest.deleteTable(tableName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}