package hdfs_api_exp.src.main.java.com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;



public class Main {

    public static void main(String[] args) {

        Configuration conf = new Configuration();

        System.setProperty("HADOOP_USER_NAME", "root");
        
        conf.set("fs.defaultFS", "hdfs://hdfs-cluster");
        conf.set("dfs.nameservices", "hdfs-cluster");
        conf.set("dfs.ha.namenodes.hdfs-cluster", "nn1,nn2");
        conf.set("dfs.namenode.rpc-address.hdfs-cluster.nn1", "119.3.208.111:9000");
        conf.set("dfs.namenode.rpc-address.hdfs-cluster.nn2", "1.92.70.157:9000");
        conf.set("dfs.client.failover.proxy.provider.hdfs-cluster", 
            "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        
        try {    
            FileSystem fs = FileSystem.newInstance(conf);
        } catch (Exception e) {
            e.printStackTrace();
            
        }
        
        
    }
        
}