package com.github.novicezk.midjourney.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class ImageDownloader {

    public static InputStream downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return url.openStream();
        } catch (IOException e) {
            log.error("下载图片失败: {}", imageUrl, e);
            throw new RuntimeException("下载图片失败, imageUrl: " + imageUrl);
        }
    }
}
