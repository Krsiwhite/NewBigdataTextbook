package hdfs_api_exp.src.main.java.com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

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


}