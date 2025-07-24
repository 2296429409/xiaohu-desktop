package com.xiaohu.fileupload.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xiaohu.fileupload.pojo.TsUploadResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaxh
 * @date 2025/7/15
 */
public class HuaBanApi {


    /**
     * 上传ts文件并返回key
     */
    public static TsUploadResult uploadTsFileToServer(String uploadUrl, String cookie, File file, int retryCount) throws IOException {
        IOException lastException = null;

        // 总共尝试次数 = 1次初始尝试 + retryCount次重试
        for (int attempt = 0; attempt < 1 + retryCount; attempt++) {
            try {
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

                URL url = new URL(uploadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setConnectTimeout(30000); // 30秒连接超时
                connection.setReadTimeout(60000); // 60秒读取超时

                if (cookie != null && !cookie.trim().isEmpty()) {
                    connection.setRequestProperty("Cookie", cookie);
                }

                try (OutputStream outputStream = connection.getOutputStream();
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

                    // 写入文件部分
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append("\r\n");
                    writer.append("Content-Type: video/mp2t").append("\r\n");
                    writer.append("\r\n");
                    writer.flush();

                    // 写入文件内容
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                    }

                    writer.append("\r\n");
                    writer.append("--").append(boundary).append("--").append("\r\n");
                    writer.flush();
                }

                // 检查响应
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP错误: " + responseCode);
                }

                // 读取响应并解析key和fileId
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String responseStr = response.toString();
                    System.out.println("上传ts文件响应: " + responseStr);

                    TsUploadResult result = parseTsUploadResponse(responseStr);
                    if (result.getKey() != null && result.getFileId() != null) {
                        return result; // 成功上传
                    } else {
                        throw new IOException("上传响应解析失败: key或fileId为空");
                    }
                }

            } catch (IOException e) {
                lastException = e;
                System.err.println("上传ts文件失败 (尝试 " + (attempt + 1) + "/" + (1 + retryCount) + "): " + file.getName() + " - " + e.getMessage());

                if (attempt < retryCount) {
                    try {
                        // 重试前等待一段时间
                        Thread.sleep(1000 * (attempt + 1)); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("上传被中断", ie);
                    }
                }
            }
        }

        // 所有重试都失败了
        throw lastException != null ? lastException : new IOException("上传失败，未知错误");
    }

    /**
     * 解析上传ts文件响应
     */
    private static TsUploadResult parseTsUploadResponse(String response) {
        try {
            JSONObject jsonObject = JSON.parseObject(response);
            if (jsonObject.containsKey("id")) {
                return new TsUploadResult(jsonObject.getString("key"), jsonObject.getLong("id"));
            }
        } catch (Exception e) {
            System.err.println("解析ts文件上传响应失败: " + e.getMessage());
        }
        return new TsUploadResult(null, null);
    }

    /**
     * 保存文件到画板
     */
    public static void saveFilesToBoard(String cookie, String boardId, List<Long> fileIds) throws IOException {
        // 每批最多保存20个文件
        final int BATCH_SIZE = 20;
        
        // 分批处理文件
        for (int i = 0; i < fileIds.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, fileIds.size());
            List<Long> batchFileIds = fileIds.subList(i, endIndex);
            
            System.out.println("正在保存第 " + (i / BATCH_SIZE + 1) + " 批文件，共 " + batchFileIds.size() + " 个文件");
            saveBatchToBoard(cookie, boardId, batchFileIds);

            // 如果不是最后一批，等待一下再继续
            if (endIndex < fileIds.size()) {
                try {
                    Thread.sleep(1000); // 等待1秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("保存过程被中断", e);
                }
            }
        }
    }
    
    /**
     * 保存一批文件到画板（单次最多20个）
     */
    private static void saveBatchToBoard(String cookie, String boardId, List<Long> fileIds) throws IOException {
        URL url = new URL("https://huaban.com/v3/pins/batch");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("referer", "https://huaban.com/boards");

        if (cookie != null && !cookie.trim().isEmpty()) {
            connection.setRequestProperty("Cookie", cookie);
        }

        // 构建请求体
        List<Map<String, Object>> pins = new ArrayList<>();
        for (Long fileId : fileIds) {
            Map<String, Object> map = new HashMap<>();
            map.put("file_id", fileId);
            pins.add(map);
        }
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("board_id", boardId);
        requestBody.put("pins", pins);

        // 发送请求
        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {
            writer.write(JSON.toJSONString(requestBody));
            writer.flush();
        }

        // 检查响应
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("保存到文件库失败，HTTP错误: " + responseCode);
        }

        // 读取响应
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            System.out.println("保存到文件库响应: " + response.toString());
        }
    }

}
