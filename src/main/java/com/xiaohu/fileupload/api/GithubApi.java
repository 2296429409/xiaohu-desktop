package com.xiaohu.fileupload.api;

import com.alibaba.fastjson2.JSON;
import com.xiaohu.fileupload.VideoDataService;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * GitHub API 工具类
 * 用于将文件上传到GitHub仓库的指定目录
 */
public class GithubApi {

    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String REPO_OWNER = "2296429409";
    private static final String REPO_NAME = "2296429409.github.io";
    private static final String FILE_PATH = "app_m3u8/";
    private static final String DB_FILE_PATH = "xiaohu.db";
    private static final String TOKEN = "";

    public static void main(String[] args) {
        try {
//            uploadFile(new File("C:\\Users\\xiaxh\\Downloads\\nacos介绍及使用_hls\\nacos介绍及使用.m3u8"));
            updateFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadFile(File file) throws Exception {
        try {
            uploadFile(Files.readAllBytes(file.toPath()), "Api添加文件", file.getName());
        } catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }
    }

    private static void uploadFile(byte[] content, String commitMessage, String fileName) throws Exception {
        String urlStr = GITHUB_API_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + FILE_PATH + fileName;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 设置请求方式为PUT
        conn.setRequestMethod("PUT");
        // 添加必要的请求头
        conn.setRequestProperty("Authorization", "token " + TOKEN);
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

        // 构建请求体
        String jsonInputString = "{"
                + "\"message\": \"" + commitMessage + "\","
                + "\"content\": \"" + java.util.Base64.getEncoder().encodeToString(content) + "\""
                + "}";

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("File uploaded successfully.");
        } else {
            System.out.println("Failed to upload file.");
        }
    }

    public static void updateFile() throws Exception {
        String database = getDatabase();
        if (database != null) {
            updateFile(Files.readAllBytes(new File(database).toPath()), "api更新文件", getFileSha());
        }
    }

    private static String getFileSha() throws Exception {
        String urlStr = GITHUB_API_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + DB_FILE_PATH;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("Authorization", "token " + TOKEN);
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            String responseBody = scanner.hasNext() ? scanner.next() : "";
            // 解析JSON响应以获取"sha"字段
            return JSON.parseObject(responseBody).getString("sha");
        } else {
            throw new Exception("Failed to get file SHA, response code: " + responseCode);
        }
    }

    private static void updateFile(byte[] content, String commitMessage, String sha) throws Exception {
        String urlStr = GITHUB_API_URL + "/repos/" + REPO_OWNER + "/" + REPO_NAME + "/contents/" + DB_FILE_PATH;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "token " + TOKEN);
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

        String jsonInputString = "{"
                + "\"message\": \"" + commitMessage + "\","
                + "\"content\": \"" + java.util.Base64.getEncoder().encodeToString(content) + "\","
                + "\"sha\": \"" + sha + "\""
                + "}";

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            System.out.println("File updated successfully.");
        } else {
            System.out.println("Failed to update file.");
        }
    }

    /**
     * 获取数据库URL
     * 开发时使用resources目录下的xiaohu.db
     * 运行时使用jar目录下的xiaohu.db
     */
    private static String getDatabase() {
        try {
            // 首先尝试从jar目录加载数据库文件
            String jarDir = getJarDirectory();
            String dbPath = jarDir + File.separator + "xiaohu.db";
            File dbFile = new File(dbPath);
            if (dbFile.exists()) {
                System.out.println("使用jar目录下的数据库: " + dbPath);
                return dbPath;
            }
        } catch (Exception e) {
            System.err.println("获取数据库路径失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取jar文件所在目录
     */
    private static String getJarDirectory() {
        try {
            // 获取当前类的位置
            String classPath = VideoDataService.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File jarFile = new File(classPath);
            if (jarFile.isFile()) {
                // 如果是jar文件，返回jar文件所在目录
                return jarFile.getParent();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
