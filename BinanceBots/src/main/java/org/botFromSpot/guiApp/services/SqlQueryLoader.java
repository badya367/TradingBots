package org.botFromSpot.guiApp.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
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
                if (resource.isFile() && resource.getFile().getName().equals(name)) {
                    return String.join("\n", Files.readAllLines(resource.getFile().toPath()));
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
