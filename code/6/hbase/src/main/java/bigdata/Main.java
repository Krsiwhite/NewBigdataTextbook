package bigdata;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        String bootstrapServers = "m1:9092,m2:9092,m3:9092"; // Kafka Broker地址

        // 创建 Storm 拓扑构建器
        TopologyBuilder builder = new TopologyBuilder();

        // 配置 KafkaSpout：读取 Kafka 中主题 filtered_log 的数据
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(bootstrapServers, "filtered_log")
                .setProp("group.id", "hbase-group")
                .build();
        builder.setSpout("kafkaSpout", new KafkaSpout<>(kafkaSpoutConfig));// 添加 Spout

        // 添加处理 Bolt（提取 timestamp 和 action）
        builder.setBolt("processBolt", new ProcessBolt()).shuffleGrouping("kafkaSpout");

        // 添加 HBase 存储 Bolt（将数据写入 HBase）
        builder.setBolt("hbaseBolt", new HBaseStorageBolt()).shuffleGrouping("processBolt");

        // 创建并配置 Storm 配置对象
        Config config = new Config();
        config.setDebug(true);// 打开调试模式

        // 提交拓扑到 Storm 集群
        StormSubmitter.submitTopology("hbase", config, builder.createTopology());
        System.out.println("拓扑提交成功");
    }
}

// 自定义FilterBolt类
class ProcessBolt extends BaseBasicBolt {
    @Override
    public void execute(Tuple input, BasicOutputCollector basicOutputCollector) {
        // 从 Kafka 消息中取出日志内容
        String logMessage = input.getStringByField("value");

        if (logMessage != null) {
            String infos[] = logMessage.split("\\t");// 使用制表符分割日志字段

            // 示例日志结构:
            // IP     地区    日期     时间戳         用户ID                  网站         操作
            // 180... 贵州   2018-..  1545...    5270...        www.xxx.com   Buy
            // 我们只取时间戳 infos[3] 和用户行为 infos[6]
            basicOutputCollector.emit(new Values(infos[3], infos[6]));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // 输出字段名（使用 BOLT_MESSAGE）
        outputFieldsDeclarer.declare(new Fields(FieldNameBasedTupleToKafkaMapper.BOLT_MESSAGE));
    }


}
// 自定义 Bolt：将日志中的时间戳与用户操作写入 HBase
class HBaseStorageBolt extends BaseRichBolt {
    private Connection connection;// HBase 连接
    private Table table;// HBase 表对象

    // 初始化方法：建立 HBase 连接
    @Override
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
        try {
            connection = ConnectionFactory.createConnection(HBaseConfiguration.create());
            table = connection.getTable(org.apache.hadoop.hbase.TableName.valueOf("web"));// 使用 web 表

        } catch (Exception e) {
            throw new RuntimeException("无法连接到HBase", e);
        }
    }

    // 核心处理方法：将数据写入 HBase
    @Override
    public void execute(Tuple tuple) {
        try {
            if (tuple != null) {
                String time = tuple.getString(0);// 时间戳
                String userAction = tuple.getString(1);// 用户操作行为

                    // 构造 HBase 的 Put 对象，RowKey 使用 操作_时间戳，例如 Buy_1545290594752
                    Put put = new Put(Bytes.toBytes(userAction+"_"+time));
                    put.addColumn(Bytes.toBytes("cf"), // 列族
                            Bytes.toBytes(userAction),// 列名（行为名）
                            Bytes.toBytes( time ));// 值（时间戳）
                    table.put(put);// 写入 HBase

            }

        } catch (Exception e) {
            throw new RuntimeException("无法将数据存入Hbase", e);
        }
    }


    // 不声明输出字段（最终 Bolt）
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // No output fields to declare for HBase storage
    }

    // 清理资源
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
