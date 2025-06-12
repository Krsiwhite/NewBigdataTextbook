package com.example;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class AlphabeticalPartitioner extends Partitioner<Text, LongWritable> {

    @Override
    public int getPartition(Text key, LongWritable value, int numPartitions) {
        // 获取单词的第一个字符
        String word = key.toString();
        if (word == null || word.isEmpty()) {
            return 0; // 如果单词为空或null，返回第0个分区
        }
        char firstChar = Character.toUpperCase(word.charAt(0));

        // 计算分区编号，A-Z 对应 0-25
        if (firstChar >= 'A' && firstChar <= 'Z') {
            return firstChar - 'A';
        } else {
            // 对于非A-Z的字符，可以将其分配到最后一个分区或者特殊处理
            return 25; // 这里我们将它们分配到最后一个分区
        }
    }
}