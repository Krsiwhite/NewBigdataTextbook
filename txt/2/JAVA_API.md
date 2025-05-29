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

# 2. HDFS
### JAVA API操作HDFS

HDFS（Hadoop Distributed File System）是Hadoop生态系统的核心组件，基于Java开发。通过Java API，我们不仅可以连接HDFS，还能实现多种文件操作，比如创建文件、下载文件以及目录的管理等。接下来，我们将分别介绍如何使用Java API完成这四项关键操作：

1. Java API 连接 HDFS
2. Java API 新建目录
3. Java API 上传文件
4. Java API 下载文件

#### 2.1. java api连接HDFS文件系统

​	Hadoop 提供了丰富的 Java API 来连接和操作 HDFS，通过 Java 程序与 HDFS 建立连接，像访问本地文件系统一样访问分布式文件系统，为大数据应用打下基础。

​	在一台服务器上启动`HDFS`。

```
root@m1:~# start-dfs.sh 
```

​	在三台服务器上分别执行`jps`命令。

```
root@m1:~# jps
2656 NameNode
2809 DataNode
3083 JournalNode
17421 Jps
```

```
root@m2:~# jps
12503 Jps
2408 DataNode
2298 NameNode
2541 JournalNode
```

```
root@m3:~# jps
10868 Jps
2294 DataNode
2423 JournalNode
```

​	创建项目。将新项目命名为`hdfs`，项目类型选择`Maven`，并选择安装的`JDK`版本。

​	接着，导入所需以来，修改项⽬根⽬录下的`pom.xml`⽂件，需要添加以下三个依赖`hadoop-common`、`hadoop-client`以及`hadoop-hdfs`。

```
<dependencies>
    <dependency>
        <groupId>org.apache.hadoop</groupId>
        <artifactId>hadoop-common</artifactId>
        <version>3.3.6</version>
    </dependency>

    <dependency>
        <groupId>org.apache.hadoop</groupId>
        <artifactId>hadoop-client</artifactId>
        <version>3.3.6</version>
    </dependency>

    <dependency>
        <groupId>org.apache.hadoop</groupId>
        <artifactId>hadoop-hdfs</artifactId>
        <version>3.3.6</version>
    </dependency>
</dependencies>
```

​	现在，我们可以开始基于HDFS的API进行编码开发了！

​	编写`HDFSConnection类`。

```java
public class HDFSConnection {

    public static void main(String[] args) {

        Configuration conf = new Configuration();

        System.setProperty("HADOOP_USER_NAME", "root");

        conf.set("fs.defaultFS", "hdfs://hdfs-cluster");
        conf.set("dfs.nameservices", "hdfs-cluster");
        conf.set("dfs.ha.namenodes.hdfs-cluster", "nn1,nn2");
        conf.set("dfs.namenode.rpc-address.hdfs-cluster.nn1", "124.71.159.110:9000");
        conf.set("dfs.namenode.rpc-address.hdfs-cluster.nn2", "1.94.115.238:9000");
        conf.set("dfs.client.failover.proxy.provider.hdfs-cluster",
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");

        try {
            FileSystem fs = FileSystem.newInstance(conf);
            System.out.println("Connected to HDFS successfully!");

            // 列举根目录文件/文件夹
            FileStatus[] status = fs.listStatus(new Path("/"));
            System.out.println("Root directory contents:");
            for (FileStatus fileStatus : status) {
                System.out.println(fileStatus.getPath().toString());
            }

            fs.close();

        } catch (Exception e) {
            System.err.println("Failed to connect to HDFS:");
            e.printStackTrace();
        }
    }

```

​	在本地编译器直接运行，可以看到，运行成功！

```
Connected to HDFS successfully!
```

#### 2.2. Java API 新建目录

​	成功建立连接后，可以开始进行新建目录操作了！

​	按照上述提及的创建java类文件`HDFSCreateDir`，在`HDFSConnection`的基础上，添加需要创建的目录地址`srcPath，使用`ensureParentDirectory`函数创建目录。

```
 FileSystem fs = FileSystem.newInstance(conf);
System.out.println("Connected to HDFS successfully!");

String srcPath = "/Hello_HDFS";

ensureParentDirectory(fs, srcPath);

fs.close();
```

​	其中，创建目录的函数`ensureParentDirectory`如下。

```java
   public static void ensureParentDirectory(FileSystem fs, String hdfsPath) throws IOException {
        Path dstPath = new Path(hdfsPath);
        Path parentPath = dstPath.getParent();
        if (!fs.exists(parentPath)) {
            fs.mkdirs(parentPath);
            System.out.println("Created directory: " + parentPath.toString());
        }
    }
```

​	在本地编译器中，直接运行，可以看到，连接成功，并且已经成功创建目录。

```
Connected to HDFS successfully!
Created directory: /Hello_HDFS
```

#### 2.3. Java API 上传文件

​	成功建立连接后，可以开始进行上传文件操作了！

​	按照上述提及的创建java类文件`HDFSWriterWithHA`，在`HDFSConnection`的基础上，添加需要上传的文件地址`localSrc`以及上传到`HDFS`的路径`hdfsDst`，并根据`dfsDst`创建目录，使用`ensureParentDirectory`函数确保 HDFS 父目录存在。最终根据`uploadFileToHDFS`函数进行上传文件操作。

```java
FileSystem fs = FileSystem.newInstance(conf);
System.out.println("Connected to HDFS successfully!");

String localSrc = "/root/file1.txt";  
String hdfsDst = "/file2.txt";       
ensureParentDirectory(fs, hdfsDst);
uploadFileToHDFS(fs, localSrc, hdfsDst);

fs.close();
```

​	最为关键的`uploadFileToHDFS`，使用`FileSystem`中的`copyFromLocalFile`将本地文件上传到HDFS文件系统中。

```java
public static void uploadFileToHDFS(FileSystem fs, String localSrc, String hdfsDst) throws IOException {
        Path srcPath = new Path(localSrc);
        Path dstPath = new Path(hdfsDst);

        if (fs.exists(dstPath)) {
            fs.delete(dstPath, false);
            System.out.println("Deleted existing file at " + hdfsDst);
        }
        
        fs.copyFromLocalFile(srcPath, dstPath);
        System.out.println("File uploaded to HDFS: " + hdfsDst);
    }
```

​	在本地编译器中，直接运行，可以看到，运行成功，并且已经将文件上传至HDFS文件系统中。

```
Connected to HDFS successfully!
File uploaded to HDFS: /file2.txt
```

​	通过`Haddop fs -ls`进行查看`HDFS`文件系统的目录。可以看到`file2.txt`在`HDFS`文件系统的目录下。

```
abaola@abaoladeMacBook-Air hdfs % hadoop fs -ls /
Found 3 items
-rw-r--r--   3 root supergroup         39 2025-05-28 16:47 /file2.txt
drwxr-xr-x   - root supergroup          0 2025-05-27 16:18 /hbase
drwxr-xr-x   - root supergroup          0 2025-05-22 22:28 /hello
```

​	通过`Hadoop fs -cat /file2.txt`再一次进行验证。可以看到`file2.txt`的内容被打印出来。

```
abaola@abaoladeMacBook-Air hdfs % hadoop fs -cat /file2.txt
Hello HDFS
```

​	通过上述两个步骤，验证了`file2.txt`文件已经被成功上传！	

#### 2.4. Java API 下载文件

​	成功建立连接后，可以开始进行下载文件操作了！

​	按照上述提及的创建java类文件`HDFSDownload`，在`HDFSConnection`的基础上，添加上传到`HDFS`的路径`hdfsDst`，根据`downloadFileFromHDFS`函数进行上传文件操作。	

```java
FileSystem fs = FileSystem.newInstance(conf);
System.out.println("Connected to HDFS successfully!");

String hdfsDst = "/file2.txt";            // HDFS 目标路径
String localDownloadPath = "/root/file2.txt";
downloadFileFromHDFS(fs, hdfsDst, localDownloadPath);

fs.close();
```

​	最为关键的`downloadFileFromHDFS`，使用`FileSystem`中的`copyToLocalFile`将HDFS为文件系统中的文件下载到本地。

```java
public static void downloadFileFromHDFS(FileSystem fs, String hdfsSrc, String localDst) throws IOException {
        Path srcPath = new Path(hdfsSrc);
        Path dstPath = new Path(localDst);

        if (!fs.exists(srcPath)) {
            System.err.println("Source file does not exist in HDFS: " + hdfsSrc);
            return;
        }

        fs.copyToLocalFile(false, srcPath, dstPath);
        System.out.println("File downloaded from HDFS to local path: " + localDst);
    }
```

​	在本地编译器中，直接运行，可以看到，运行成功，并且已经将`file2.txt`文件下载到服务器上了！

```
Connected to HDFS successfully!
File downloaded from HDFS to local path: /Users/abaola/Desktop/hdfs/file2.txt
```

​	通过`cat file2.txt`再次验证，可以看到HDFS上的指定文件已经被下载到本地了。

```
abaola@abaoladeMacBook-Air hdfs % cat file2.txt 
Hello HDFS
```

#### 