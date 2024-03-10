package org.botFromSpot.guiApp.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class SqlQueryLoader {

    private ResourceLoader resourceLoader;

    public static String loadSql(String name) {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            List<Resource> list = Arrays.asList(resourcePatternResolver.getResources("classpath:sqlScripts/*"));
            for (Resource resource : list) {
                if (resource.getFilename().equals(name)) {
                    byte[] binaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
                    String s = new String(binaryData, StandardCharsets.UTF_8);
                    return s;
                }
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
