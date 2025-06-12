package bigdatatextbook;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import java.util.Map;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class KafkaStormHBaseTopology {


    /**
     * HBase存储Bolt类，用于将数据存储到HBase表中
     */
    public static class HBaseStorageBolt extends BaseRichBolt {
        private Connection connection;// HBase连接
        private Table table;// HBase表

        /**
         * 在Bolt初始化时，建立HBase连接并获取表
         */
        @Override
        public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
            try {
                // 创建HBase连接
                connection = ConnectionFactory.createConnection(HBaseConfiguration.create());

                // 获取HBase表，表名为"storm_test"
                table = connection.getTable(org.apache.hadoop.hbase.TableName.valueOf("storm_test"));
            } catch (Exception e) {
                throw new RuntimeException("无法连接到HBase", e);
            }
        }

        /**
         * 处理每个从Kafka接收到的消息，并将其存储到HBase表中
         */
        @Override
        public void execute(Tuple tuple) {
            try {
                // 从元组中获取消息内容并转为小写
                String value = tuple.getStringByField("value").toLowerCase();

                // 使用"row_时间戳"作为row key，避免重复
                String rowKey = "row_" + System.currentTimeMillis();

                // 创建Put操作，将数据写入HBase表
                Put put = new Put(Bytes.toBytes(rowKey));
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("qualifier"), Bytes.toBytes(value));
                table.put(put);
            } catch (Exception e) {
                throw new RuntimeException("无法将数据存入Hbase", e);
            }
        }


        /**
         * 声明输出字段，由于该Bolt不输出数据，因此无需声明
         */
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            // No output fields to declare for HBase storage
        }

        /**
         * 在Bolt关闭时，释放HBase资源
         */
        @Override
        public void cleanup() {
            try {
                if (table != null) table.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                throw new RuntimeException("无法关闭Hbase资源", e);
            }
        }
    }

    /**
     * 构建Storm拓扑
     * @param bootstrapServers Kafka的Bootstrap服务器地址
     * @param topic Kafka主题
     * @return 构建好的Storm拓扑
     */
    public static StormTopology buildTopology(String bootstrapServers, String topic) {
        // 构建KafkaSpout配置
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(bootstrapServers, topic)
                .setProp("group.id", "storm-kafka-group")  // 设置Kafka消费者组ID
                .build();

        // 创建拓扑构建器
        TopologyBuilder builder = new TopologyBuilder();

        // 添加KafkaSpout到拓扑，名称为"kafka-spout"
        builder.setSpout("kafka-spout", new KafkaSpout<>(kafkaSpoutConfig));

        // 添加HBase存储Bolt到拓扑，名称为"hbase-storage-bolt"，并将其与KafkaSpout连接
        builder.setBolt("hbase-storage-bolt", new HBaseStorageBolt()).shuffleGrouping("kafka-spout");

        // 返回构建好的拓扑
        return builder.createTopology();
    }


    /**
     * 主方法，用于提交Storm拓扑到集群
     */
    public static void main(String[] args) throws Exception {
        // Kafka的Bootstrap服务器地址
        String bootstrapServers = "m1:9092,m2:9092,m3:9092";

        // Kafka主题
        String topic = "mylog";

        // 构建Storm拓扑
        StormTopology topology = buildTopology(bootstrapServers, topic);

        // 创建Storm配置
        Config config = new Config();
        config.setDebug(true);// 开启调试模式

        // 提交拓扑到Storm集群
        StormSubmitter.submitTopology(
                "storm_topology",// 拓扑名称
                config,// 配置
                topology// 拓扑
        );
        System.out.println("拓扑提交成功");
    }
}