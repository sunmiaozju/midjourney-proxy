package com.github.novicezk.midjourney.util;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

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

    public static URL putObjectToOss(String fileName, InputStream input) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        log.info("starting putObjectToOss fileName={}", fileName);
        PutObjectResult result = ossClient.putObject(bucketName, fileName, input, metadata);
        if (result.getETag() == null) {
            log.error("上传失败, objectKey: {}", fileName);
            throw new RuntimeException("上传失败, objectKey: " + fileName);
        }
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileName);
        // 设置HttpMethod为GET。
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        // 设置签名URL过期时间, 7天过期
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DATE, 7);
        Date expiration = cal.getTime();
        generatePresignedUrlRequest.setExpiration(expiration);
        // 下载后显示为原Object名称。
        Map<String, String> queryParam = new HashMap<>();
        queryParam.put("response-content-disposition",  "inline");
        generatePresignedUrlRequest.setQueryParameter(queryParam);
        // 生成签名URL。
        return ossClient.generatePresignedUrl(generatePresignedUrlRequest);

    }

    public static String transToOssUrl(String imageUrl) {
        InputStream inputStream = ImageDownloader.downloadImage(imageUrl);
        // 提取图片文件名称
        String fileName = System.currentTimeMillis() + ".png";
        URL url = OssUploader.putObjectToOss(fileName, inputStream);
        return url.toString();
    }
}