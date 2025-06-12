package com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.io.IOException;

public class HDFSCreateDir {

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

            String srcPath = "/Hello_HDFS";

            ensureParentDirectory(fs, srcPath);

            fs.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void ensureParentDirectory(FileSystem fs, String hdfsPath) throws IOException {
        Path dstPath = new Path(hdfsPath);
        Path parentPath = dstPath.getParent();
        if (!fs.exists(parentPath)) {
            fs.mkdirs(parentPath);
            System.out.println("Created directory: " + parentPath.toString());
        }
    }
}
