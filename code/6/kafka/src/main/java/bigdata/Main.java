package bigdata;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.thrift.TException;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        // Kafka Broker 列表（m1、m2、m3 是 Kafka 集群中的主机名）
        String bootstrapServers = "m1:9092,m2:9092,m3:9092";

        // 创建 Storm 拓扑构建器
        TopologyBuilder builder = new TopologyBuilder();

        // 配置 KafkaSpout，用于从 Kafka 的 "all_my_log" 主题中读取消息
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(bootstrapServers, "all_my_log")
                .setProp("group.id", "kafka-group")// 设置消费者组 ID
                .build();

        // 将 KafkaSpout 添加到拓扑中，名字为 kafkaSpout
        builder.setSpout("kafkaSpout", new KafkaSpout<>(kafkaSpoutConfig));

        // 添加自定义的 FilterBolt（过滤 Bolt），从 kafkaSpout 接收数据
        builder.setBolt("filterBolt", new FilterBolt()).shuffleGrouping("kafkaSpout");

        // KafkaBolt 用于将处理后的数据写回 Kafka（写入 filtered_log 主题）
        Properties props = new Properties();
        props.put("bootstrap.servers", "m1:9092,m2:9092,m3:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 配置 KafkaBolt：指定目标主题，字段映射方式，Kafka 生产者参数
        KafkaBolt<String, String> kafkaBolt = new KafkaBolt<String, String>()
                .withTopicSelector(new DefaultTopicSelector("filtered_log"))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<>())
                .withProducerProperties(props);

        // 将 kafkaBolt 添加到拓扑，接收来自 filterBolt 的数据
        builder.setBolt("kafkaBolt", kafkaBolt).shuffleGrouping("filterBolt");

        // 配置 Storm 参数
        Config config = new Config();
        config.setDebug(true);// 打开调试模式

        // 提交拓扑到集群中运行，拓扑名称为 "kafka"
        StormSubmitter.submitTopology("kafka", config, builder.createTopology());

        System.out.println("拓扑提交成功");
    }
}

// 自定义的过滤 Bolt，用于筛选包含 "Buy" 或 "Shopping_Car" 的日志
class FilterBolt extends BaseBasicBolt {
    @Override
    public void execute(Tuple input, BasicOutputCollector basicOutputCollector) {
        // 从 Kafka 消息中提取 value 字段（KafkaSpout 默认会包含 key 和 value 字段）
        String logMessage = input.getStringByField("value");

        // 过滤逻辑：只保留包含 "Buy" 或 "Shopping_Car" 的日志
        if (logMessage.contains("Buy") || logMessage.contains("Shopping_Car")) {

            // 发射过滤后的日志，字段名为 "message"（FieldNameBasedTupleToKafkaMapper 会自动识别）
            basicOutputCollector.emit(new Values(logMessage));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        // 声明输出字段名，KafkaBolt 默认使用 "message" 作为 value 字段
        outputFieldsDeclarer.declare(new Fields(FieldNameBasedTupleToKafkaMapper.BOLT_MESSAGE));
    }


}
