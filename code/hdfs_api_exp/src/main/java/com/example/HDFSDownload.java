package com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HDFSDownload {

    public static void main(String[] args) {
        Configuration conf = new Configuration();

        // 设置 HDFS 用户
        System.setProperty("HADOOP_USER_NAME", "root");

        // 配置 HDFS 高可用集群信息
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


            // 下载文件
            String hdfsDst = "/file2.txt";            // HDFS 目标路径
            String localDownloadPath = "/root/downloaded_file2.txt";
            downloadFileFromHDFS(fs, hdfsDst, localDownloadPath);

            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 HDFS 下载文件到本地
     */
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

    /**
     * 确保 HDFS 父目录存在
     */
    public static void ensureParentDirectory(FileSystem fs, String hdfsPath) throws IOException {
        Path dstPath = new Path(hdfsPath);
        Path parentPath = dstPath.getParent();
        if (!fs.exists(parentPath)) {
            fs.mkdirs(parentPath);
            System.out.println("Created directory: " + parentPath.toString());
        }
    }
}
