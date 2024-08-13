package com.eyre.community.util;

import com.alibaba.fastjson.JSONObject;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommunityUtil.class);

    private static final String ACCESS_KEY = "ZWBT2JUYRONYFCQUR6N3";

    private static final String SECRET_KEY = "tnO0IyFdweykZHFg7MZnj83U4045ADiS3F1BGJYu";

    private static final String ENDPOINT = "obs.cn-south-1.myhuaweicloud.com";

    private static final String HEADER_BUCKET_NAME = "header-eyre";

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // hello -> abc123def456
    // hello + 3e4a8 -> abc123def456abc
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static String upload(byte[] bytes, String objectName) {
        // 创建OSSClient实例。
        ObsClient obsClient = new ObsClient(ACCESS_KEY, SECRET_KEY, ENDPOINT);

        try {
            // 创建PutObject请求。
            obsClient.putObject(HEADER_BUCKET_NAME, objectName, new ByteArrayInputStream(bytes));
            logger.info("云端上传文件成功：" + objectName);
        } catch (ObsException e) {
            logger.error("putObject failed");
            // 请求失败,打印http状态码
            logger.error("HTTP Code:" + e.getResponseCode());
            // 请求失败,打印服务端错误码
            logger.error("Error Code:" + e.getErrorCode());
            // 请求失败,打印详细错误信息
            logger.error("Error Message:" + e.getErrorMessage());
            // 请求失败,打印请求id
            logger.error("Request ID:" + e.getErrorRequestId());
            logger.error("Host ID:" + e.getErrorHostId());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName https://桶名.域名/文件夹目录层级/对象名
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(HEADER_BUCKET_NAME)
                .append(".")
                .append(ENDPOINT)
                .append("/")
                .append(objectName);

        logger.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
