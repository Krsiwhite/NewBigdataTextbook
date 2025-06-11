package bigdata;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Main {


    public static void main(String[] args) {
        // 创建一个 RPC 客户端，连接到本地 Flume Agent 的 4141 端口
        RpcClient client = RpcClientFactory.getDefaultInstance("localhost", 4141);

        // 创建随机数生成器
        Random random = new Random();

        // 用户可能的行为列表
        String[] action_list = { "Register", "Login", "View", "Click", "Double_Click", "Buy", "Shopping_Car", "Add", "Edit", "Delete", "Comment", "Logout" };

        // IP 地址生成的上限
        int maxIpNumber=224;

        // 地址（省份、直辖市、自治区）列表
        String[] address_list = { "北京", "天津", "上海", "广东", "重庆", "河北", "山东", "河南", "云南", "山西", "甘肃", "安徽", "福建", "黑龙江", "海南", "四川", "贵州", "宁夏", "新疆", "湖北", "湖南", "山西", "辽宁", "吉林", "江苏", "浙江", "青海", "江西", "西藏", "内蒙", "广西", "香港", "澳门", "台湾", };

        // 模拟网站地址
        String web_site="www.xxx.com";

        try {
            while (true) {
                // 获取当前系统时间并格式化为字符串
                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String d = simpleDateFormat.format(date);

                // 获取当前时间戳（毫秒）
                Long timestamp = new Date().getTime();

                // 生成随机 IP 地址
                String ip = random.nextInt(maxIpNumber) + "." + random.nextInt(maxIpNumber) + "." + random.nextInt(maxIpNumber) + "." + random.nextInt(maxIpNumber);

                // 随机选择一个地址（地理位置）
                String address = address_list[random.nextInt(address_list.length)];

                // 生成一个正整数的用户 ID
                Long userid = Math.abs(random.nextLong());

                // 随机选择一个用户行为
                String action = action_list[random.nextInt(action_list.length)];

                // 构造日志数据：IP 地址、地址、时间、时间戳、用户 ID、网站、行为（用制表符分隔）
                // 示例：199.80.45.117 云南 2018-12-20 1545285957720 3086250439781555145 www.xxx.com Buy
                String data = ip + "\t" + address + "\t" + d + "\t" + timestamp + "\t" + userid + "\t" + web_site + "\t" + action;

                // 创建一个 Flume 事件，设置日志内容和编码
                Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"));

                // 发送事件到 Flume
                client.append(event);

                // 每隔 1 秒发送一次数据
                Thread.sleep(1000);
            }
        } catch (EventDeliveryException e) {
            // 处理 Flume 发送失败的异常
            System.err.println("Failed to send event to Flume: " + e.getMessage());
        } catch (InterruptedException e) {
            // 处理线程中断异常
            System.err.println("Thread was interrupted: " + e.getMessage());
        } finally {
            // 最终关闭 RPC 客户端连接
            client.close();
        }
    }
}