package com.darian.financemanagement.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    /**
     * Đọc file từ classpath hoặc file system.
     * Thử classpath trước, nếu không tìm thấy thì thử file system.
     * 
     * @param filePath Đường dẫn file (có thể bắt đầu bằng / hoặc không)
     * @return InputStream của file
     * @throws IOException Nếu không tìm thấy file ở cả 2 nơi
     */
    public static InputStream getInputStream(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IOException("File path is null or empty");
        }

        // Nếu path bắt đầu bằng /, bỏ dấu / để dùng với getResourceAsStream
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        // Thử đọc từ classpath trước
        InputStream in = FileUtils.class.getClassLoader().getResourceAsStream(filePath);

        if (in != null) {
            System.out.println("Found file in classpath: " + filePath);
            return in;
        }

        // Nếu không tìm thấy trong classpath, thử đọc từ file system
        File file = new File(filePath);
        if (file.exists()) {
            System.out.println("Found file in file system: " + file.getAbsolutePath());
            return new FileInputStream(file);
        }

        throw new IOException("File not found in classpath or file system: " + filePath);
    }
}
