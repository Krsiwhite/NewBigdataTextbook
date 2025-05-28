# 1. HBase（IDEA为例）
## 1.1 准备工作
&emsp;&emsp;编辑本机电脑的 host 文件（windows系统修改c：\Windows\System32\drivers\etc\hosts，mac和Linux系统修改/etc/hosts，请谨慎编辑，避免格式错误!!!），添加 host 映射 ：
```shell
# 注意要将 ip 地址改为对应服务器的公网 ip
111.1.1.1 m1
111.1.1.2 m2
111.1.1.3 m3
```
## 1.2 导入 maven 依赖
&emsp;&emsp;在IDEA中创建好项目后，添加Hbase客户端依赖：
```xml
<dependency>
    <groupId>org.apache.hbase</groupId>
    <artifactId>hbase-client</artifactId>
    <version>2.0.6</version>
</dependency>
```
&emsp;&emsp;另外，为提高依赖下载速度，配置阿里云的MAVEN镜像仓库代替默认的中央仓库：
```xml
<!-- 配置阿里云仓库 --> 
<repositories>

    <repository>
        <id>aliyun-repos</id>
        <url>https://maven.aliyun.com/repository/public</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>aliyun-repos</id>
            <url>https://maven.aliyun.com/repository/public</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
```
## 1.3 代码编写
&emsp;&emsp;1. 导入相关java包：
```java
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
```
&emsp;&emsp;2. 定义配置文件的静态变量用于连接HBase集群,并设置Zookeeper集群地址：
```java
static Configuration conf = HBaseConfiguration.create();
conf.set("hbase.zookeeper.quorum", "m1,m2,m3");
```
&emsp;&emsp;3. JAVA API操纵HBase的各种操作：
```java
/**
 * 创建HBase表
 * @param tableName 表名
 * @param columnFamilies 列族名数组
 */
Admin admin = connection.getAdmin(); // 获取HBaseAdmin对象，用于管理表
TableDescriptorBuilder tableBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName)); // 使用TableDescriptorBuilder构建表描述符
ColumnFamilyDescriptor cf = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(family)).build(); // 使用ColumnFamilyDescriptorBuilder构建列族描述符
tableBuilder.setColumnFamily(cf); // // 将列族添加到表描述符中
TableDescriptor table = tableBuilder.build(); // 创建表描述符
admin.createTable(table); // 调用createTable创建表

/**
 * 删除HBase表
 * @param tableName 要删除的表名
 */
admin.disableTable(TableName.valueOf(tableName)); // 先禁用表才能删除
admin.deleteTable(TableName.valueOf(tableName)); // 删除表tableName

/**
     * 向HBase表中插入单条数据
     * @param tableName 表名
     * @param rowKey 行键
     * @param colFamily 列族名
     * @param col 列名
     * @param value 要插入的值
     */
Table table = connection.getTable(TableName.valueOf(tableName)) // 获取table对象
Put put = new Put(Bytes.toBytes(rowKey)); // 创建Put对象，指定行键
put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col), Bytes.toBytes(value)); // 向Put对象中添加列族、列、值
table.put(put); // 执行插入操作

/**
 * 删除HBase表中特定行的特定列数据
 * @param tableName 表名
 * @param rowKey 行键
 * @param colFamily 列族名
 * @param col 要删除的列名
 */
Delete delete = new Delete(Bytes.toBytes(rowKey)); // 创建Delete对象，指定行键
delete.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(col)); // 指定要删除的列
table.delete(delete); // 执行删除操作
```
&emsp;&emsp;Note:完整程序实例请参考 github 链接。