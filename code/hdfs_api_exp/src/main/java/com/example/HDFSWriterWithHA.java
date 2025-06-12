package hdfs_api_exp.src.main.java.com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.io.IOException;

public class HDFSWriterWithHA {

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

            String localSrc = "/root/file1.txt";  // 你的本地文件
            String hdfsDst = "/file2.txt";       // HDFS上传路径，绝对路径，从根开始

            ensureParentDirectory(fs, localSrc);
            uploadFileToHDFS(fs, localSrc, hdfsDst);

            fs.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
