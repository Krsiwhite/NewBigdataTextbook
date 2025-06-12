package com.example;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {

    // 输出的 key 和 value 对象，避免重复创建对象，提高性能
    private final Text word = new Text();
    private final LongWritable one = new LongWritable(1);

    // 正则表达式：匹配字母、数字和空格，替换掉其他字符
    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        // 获取输入的一行文本
        String line = value.toString();

        // 清洗文本：去掉特殊字符，转小写
        String cleanedLine = cleanText(line);

        // 按空格分割成单词
        String[] words = cleanedLine.split("\\s+");

        // 遍历所有单词，输出 <word, 1>
        for (String wordStr : words) {
            if (!wordStr.isEmpty()) {
                word.set(wordStr);
                context.write(word, one);
            }
        }
    }

    //清洗文本的方法
    private String cleanText(String text) {
        // 替换非字母、数字和空格的字符为空格
        Matcher matcher = CLEAN_PATTERN.matcher(text);
        text = matcher.replaceAll(" ");

        // 转小写
        text = text.toLowerCase();

        // 多个空格合并为一个
        text = text.replaceAll("\\s+", " ");

        // 去除首尾空格
        return text.trim();
    }
}