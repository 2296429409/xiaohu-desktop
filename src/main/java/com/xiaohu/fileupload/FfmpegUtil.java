package com.xiaohu.fileupload;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;

/**
 * @author xiaxh
 * @date 2024/9/12
 */
public class FfmpegUtil {

    private static final String TMP_PATH = "hls";

    /**
     * 视频转ts格式
     *
     * @param videoPath 视频路径
     * @return
     */
    public static String videotoTs(String videoPath) {
        File video = new File(videoPath);
        String fileName = video.getName().substring(0, video.getName().lastIndexOf("."));
        String hlsPath = video.getParent() + File.separator + fileName + "_hls";
        File hlsFile = new File(hlsPath);
        if (!hlsFile.isDirectory()) {
            hlsFile.mkdir();
        }
        String outputPath = hlsPath + File.separator + fileName + ".ts";
        try {
            //创建FFmpeg对象
            FFmpeg ffmpeg = new FFmpeg(Loader.load(org.bytedeco.ffmpeg.ffmpeg.class));
            //创建FFmpegBuilder对象，设置推流/转码参数
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoPath)
                    .overrideOutputFiles(true)
                    .addOutput(outputPath)
                    .setAudioCodec("copy")
                    .setVideoCodec("copy")
                    .setVideoBitStreamFilter("h264_mp4toannexb")
                    .done();
            new FFmpegExecutor(ffmpeg).createJob(
                    builder,
                    progress -> System.out.println("ffmpeg: " + progress)
            ).run();
            return outputPath;
        } catch (Exception e) {
            throw new RuntimeException("videotoTs.视频转ts格式:" + e.getMessage());
        }
    }

    /**
     * ts转m3u8
     *
     * @param videoPath 视频路径
     * @return
     */
    public static String videotoM3u8(String videoPath, String segmentTime) {
        File video = new File(videoPath);
        String hlsPath = video.getParent();
        String fileName = video.getName().substring(0, video.getName().lastIndexOf("."));
        String m3u8Path = hlsPath + File.separator + fileName + ".m3u8";
        String outputPath = hlsPath + File.separator + fileName + "_%4d.ts";
        try {
            //创建FFmpeg对象
            FFmpeg ffmpeg = new FFmpeg(Loader.load(org.bytedeco.ffmpeg.ffmpeg.class));
            //创建FFmpegBuilder对象，设置推流/转码参数
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoPath)
                    .overrideOutputFiles(true)
                    .addOutput(outputPath)
                    .setFormat("segment")
                    .setAudioCodec("copy")
                    .setVideoCodec("copy")
                    .addExtraArgs("-segment_list", m3u8Path)
                    .addExtraArgs("-segment_time", segmentTime == null ? "10" : segmentTime)
                    .done();
            new FFmpegExecutor(ffmpeg).createJob(
                    builder,
                    progress -> System.out.println("ffmpeg: " + progress)
            ).run();
            return m3u8Path;
        } catch (Exception e) {
            throw new RuntimeException("videotoM3u8.ts转m3u8:" + e.getMessage());
        }
    }

    /**
     * 获取视频合适的segmentTime参数
     *
     * @param videoPath 视频路径
     * @return
     */
    public static String getSegmentTime(String videoPath) {
        // 获取视频时长
        double duration = 0l;
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            duration = grabber.getLengthInTime() / 1000000.0;
            grabber.stop();
        } catch (Exception e) {
            throw new RuntimeException("getSegmentTime");
        }
        // 指定文件路径
        File file = new File(videoPath);
        // 获取文件大小（字节）
        long fileSizeInBytes = file.length();
        // 将文件大小转换为 MB
        double fileSizeInMB = (double) fileSizeInBytes / (1024 * 1024);
        int i = (int) (duration / (fileSizeInMB / 10));
        return String.valueOf(i);
    }

    /**
     * 验证文件不能大于 sizeThreshold
     *
     * @param directory     文件路径
     * @param sizeThreshold 大小
     */
    public static void findLargeJpgFiles(File directory, long sizeThreshold) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".ts"));
            if (files != null) {
                for (File file : files) {
                    if (file.length() > sizeThreshold) {
                        throw new RuntimeException("findLargeJpgFiles: file.length() > sizeThreshold");
                    }
                }
            }
        } else {
            throw new RuntimeException("findLargeJpgFiles.文件夹不存在或无效路径");
        }
    }

    public static String run(String videoPath) {
        //先转格式
        String ts = videotoTs(videoPath);
        //切片
        String m3u8File = videotoM3u8(ts, getSegmentTime(videoPath));
        //删除ts文件
        File tsFile = new File(ts);
        tsFile.delete();
        //验证文件大小
        findLargeJpgFiles(tsFile.getParentFile(), 18 * 1024 * 1024);
        return m3u8File;
    }

}
