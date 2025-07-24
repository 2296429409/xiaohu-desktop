package com.xiaohu.fileupload;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author xiaxh
 * @date 2025/7/15
 */
public class FileHandle {

    public final static String imgKeyStr = "\n---data---\n";
    private final static String server = "https://gd-hbimg.huaban.com/%s?key=%s";

    /**
     * 对单个ts文件进行加密
     */
    public static File encryptTsFile(File tsFile, File keyFile) throws IOException {
        String nameTs = tsFile.getName().substring(0, tsFile.getName().lastIndexOf("."));
        byte[] bytek = imgKeyStr.getBytes();
        String imagePath = tsFile.getParent() + "/" + nameTs + ".jpg";

        try (
                BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(imagePath)));
                FileInputStream fis = new FileInputStream(keyFile);
                final BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(tsFile.toPath()));
        ) {
            // 写入密钥文件内容
            byte[] bytes = new byte[10240];
            int read = fis.read(bytes);
            bos.write(bytes, 0, read);
            bos.flush();

            // 写入分隔符
            bos.write(bytek);
            bos.flush();

            // 写入ts文件内容
            byte[] buff = new byte[1024];
            int bytesRead = 0;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
                bos.flush();
            }
        }

        return new File(imagePath);
    }

    /**
     * ts文件加密转img
     *
     * @param tsPath  ts文件路径
     * @param imgWord 密钥文件
     * @return
     */
    public static List<String> fileChangeImg(String tsPath, File imgWord) {
        List<String> imgList = new ArrayList<>();
        File file = new File(tsPath);
        for (int i = 0; i < file.listFiles().length; i++) {
            File tsFile = file.listFiles()[i];
            if (!tsFile.getName().contains(".m3u8")) {
                System.out.println("1.正在处理文件: " + tsFile.getName());
                String nameTs = tsFile.getName().substring(0, tsFile.getName().lastIndexOf("."));
                byte[] bytek = imgKeyStr.getBytes();
                String imagePath = file.getPath() + "/" + nameTs + ".jpg";
                try (
                        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(Paths.get(imagePath)));
                        FileInputStream fis = new FileInputStream(imgWord);
                        final BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(tsFile.toPath()));
                ) {
                    byte[] bytes = new byte[10240];
                    int read = fis.read(bytes);
                    bos.write(bytes, 0, read);
                    bos.flush();
                    bos.write(bytek);
                    bos.flush();
                    byte[] buff = new byte[1024];
                    int bytesRead = 0;
                    while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                        bos.write(buff, 0, bytesRead);
                        bos.flush();
                    }
                    imgList.add(imagePath);
                } catch (Exception e) {
                    System.out.println("fileChangeImg.err" + e.getMessage());
                }
            }
        }
        return imgList;
    }

    /**
     * 获取秘钥
     *
     * @param keyFile 密钥文件
     * @return
     */
    public static int getImgWordKey(File keyFile) {
        int imgKey = 0;
        try(FileInputStream fis = new FileInputStream(keyFile)){
            byte[] bytes = new byte[10240];
            int read = fis.read(bytes);
            byte[] bytek = imgKeyStr.getBytes();
            imgKey = read + bytek.length;
        }catch (Exception e){
            System.out.println("getImgWordKey.err" + e.getMessage());
        }
        return imgKey;
    }



    /**
     * 清理临时文件
     */
    public static void cleanupTempFiles(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            //删除当前目录
            directory.delete();
        }
    }

    /**
     * 获取文件列表
     */
    public static List<File> getTsFiles(File directory, String suffix) {
        List<File> tsFiles = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(suffix);
                }
            });
            if (files != null) {
                tsFiles.addAll(Arrays.asList(files));
            }
        }
        return tsFiles;
    }

    /**
     * 获取ts文件列表
     */
    public static List<File> getTsFiles(File directory) {
        return getTsFiles(directory, ".ts");
    }



    /**
     * 检查是否为视频文件
     */
    public static boolean isVideoFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
                fileName.endsWith(".mov") || fileName.endsWith(".mkv") ||
                fileName.endsWith(".wmv") || fileName.endsWith(".flv") ||
                fileName.endsWith(".webm") || fileName.endsWith(".m4v");
    }



    /**
     * 更新m3u8文件中的地址
     */
    public static void updateM3u8File(File m3u8File, Map<String, String> tsFileKeys, int imgKey) throws IOException {
        List<String> lines = Files.readAllLines(m3u8File.toPath(), StandardCharsets.UTF_8);
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().endsWith(".ts")) {
                // 找到ts文件行，替换为新的URL
                String tsFileName = line.trim();
                String key = tsFileKeys.get(tsFileName);
                if (key != null) {
                    String newUrl = String.format(server, key, imgKey);
                    updatedLines.add(newUrl);
                } else {
                    updatedLines.add(line);
                }
            } else {
                updatedLines.add(line);
            }
        }
        // 写回文件
        Files.write(m3u8File.toPath(), updatedLines, StandardCharsets.UTF_8);
        //移动文件到上一层目录
        FileUtils.moveFileToDirectory(m3u8File, m3u8File.getParentFile().getParentFile(), true);
    }
}
