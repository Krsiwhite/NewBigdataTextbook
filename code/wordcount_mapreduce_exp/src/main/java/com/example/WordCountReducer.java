package com.example;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {

    // 用于存储最终输出的 value
    private final LongWritable result = new LongWritable();

    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Context context)
            throws IOException, InterruptedException {

        long sum = 0;

        // 遍历所有值，进行累加
        for (LongWritable value : values) {
            sum += value.get(); // 累加每个单词出现的次数
        }

        // 设置结果并写出
        result.set(sum);
        context.write(key, result); // 输出 <单词, 总次数>
    }
}