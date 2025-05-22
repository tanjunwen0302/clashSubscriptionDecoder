package top.tanjunwen.clashsubscriptiondecoder.service.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import top.tanjunwen.clashsubscriptiondecoder.model.dto.ClashResultDTO;
import top.tanjunwen.clashsubscriptiondecoder.service.SubscribeToConvertService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscribeToConvertServiceImpl implements SubscribeToConvertService {
    @Override
    public ClashResultDTO getClashSubscribe(String urlString) {

        ClashResultDTO clashResultDTO = new ClashResultDTO();

        HttpURLConnection connection = null;
        byte[] rawBytes;

        try {
            System.out.println("正在从以下URL获取内容 (Fetching content from URL): " + urlString);
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Clash/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应头
                clashResultDTO.setHeaders(connection.getHeaderFields());
                System.out.println("--- 响应头结束 (End of Response Headers) ---\n");


                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                rawBytes = byteArrayOutputStream.toByteArray();
                System.out.println("成功获取内容 (Successfully fetched content).\n");
                inputStream.close();
                byteArrayOutputStream.close();
            } else {

                throw new Exception("请求失败 (Request failed). HTTP 状态码 (HTTP Status Code): " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (rawBytes == null || rawBytes.length == 0) {
            System.out.println("未能获取到任何内容 (Failed to fetch any content).");
            return null;
        }

        String[] encodingsToTry = {"UTF-8"};
        System.out.println("尝试使用不同的字符编码直接解码... (Attempting direct decoding with different charsets)...");
        for (String encoding : encodingsToTry) {
            try {
                String currentDecodedContent = new String(rawBytes, encoding);
                System.out.println("成功使用 " + encoding + " 解码 (Successfully decoded with " + encoding + ").");

                clashResultDTO.setBody(currentDecodedContent);
            } catch (UnsupportedEncodingException e) {
                System.out.println("使用 " + encoding + " 解码失败 (Decoding failed with " + encoding + "): " + e.getMessage());
            } catch (Exception e) {
                System.out.println("使用 " + encoding + " 解码时发生意外错误 (Unexpected error during decoding with " + encoding + "): " + e.getMessage());
            }
        }



        return clashResultDTO;
    }


    @Override
    public void clashDataReplacement(ClashResultDTO clashResultDTO,Integer index) {
        Yaml yaml = new Yaml();
        String clashMyConfig = ResourceUtil.readUtf8Str("static/clash_my_config.yaml");
        Map<String, Object> myConfigMap = yaml.load(clashMyConfig);
        Map<String, Object> remoteConfigMap = yaml.load(clashResultDTO.getBody());
        List<Object> proxiesNameList = (List<Object>)remoteConfigMap.get("proxies");
        proxiesNameList=proxiesNameList.stream().skip(index).toList();


        List<String> targetNodeNameList=new ArrayList<>();
        targetNodeNameList.add("DIRECT");
        proxiesNameList.forEach(e->{
             targetNodeNameList.add(JSONObject.parseObject(JSONObject.toJSONString(e)).getString("name"));
        });

        myConfigMap.put("proxies",proxiesNameList);
        List<Object> proxyGroups= (List<Object>)myConfigMap.get("proxy-groups");
        proxyGroups.forEach(e->{
            Map<String,Object> proxyGroupMap = (Map<String,Object>)e;
            proxyGroupMap.put("proxies",targetNodeNameList);
        });
//        System.out.println(targetNodeNameList);
        clashResultDTO.setBody(yaml.dump(myConfigMap));
    }
}
