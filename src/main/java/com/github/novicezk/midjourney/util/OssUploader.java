package com.github.novicezk.midjourney.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Slf4j
public class OssUploader {

    // 从环境变量获取
    private static final String accessKeyId = System.getenv("ACCESS_KEY_ID");
    private static final String accessKeySecret = System.getenv("ACCESS_KEY");
    private static final String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
    private static final String bucketName = "midjourney-pics";
    private static final OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

    public static void uploadToOss(String objectKey, InputStream inputStream) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream);
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            log.error("上传失败", e);
            throw new RuntimeException("上传失败, objectKey: " + objectKey);
        }
    }

    public static URL generatePublicUrl(String objectKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
        request.setExpiration(new Date(Long.MAX_VALUE)); // 设置过期时间为最大值，表示永不过期
        return ossClient.generatePresignedUrl(request);
    }
}