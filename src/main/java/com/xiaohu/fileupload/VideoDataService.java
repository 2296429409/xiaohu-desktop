package com.xiaohu.fileupload;

import com.xiaohu.fileupload.pojo.VideoData;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据服务类
 * 负责数据库操作
 */
public class VideoDataService {
    private static final String DB_URL = getDatabaseUrl();

    /**
     * 获取数据库URL
     * 开发时使用resources目录下的xiaohu.db
     * 运行时使用jar目录下的xiaohu.db
     */
    private static String getDatabaseUrl() {
        try {
            // 首先尝试从jar目录加载数据库文件
            String jarDir = getJarDirectory();
            String dbPath = jarDir + File.separator + "xiaohu.db";
            File dbFile = new File(dbPath);
            
            if (dbFile.exists()) {
                System.out.println("使用jar目录下的数据库: " + dbPath);
                return "jdbc:sqlite:" + dbPath;
            }
            
            // 如果jar目录下没有，尝试从resources目录复制
            String resourcePath = "/xiaohu.db";
            java.io.InputStream inputStream = VideoDataService.class.getResourceAsStream(resourcePath);
            
            if (inputStream != null) {
                // 将resources中的数据库文件复制到jar目录
                try (java.io.FileOutputStream outputStream = new java.io.FileOutputStream(dbFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                System.out.println("从resources复制数据库到jar目录: " + dbPath);
                return "jdbc:sqlite:" + dbPath;
            }
            
            // 如果都没有，使用默认路径
            System.out.println("使用默认数据库路径: xiaohu.db");
            return "jdbc:sqlite:xiaohu.db";
            
        } catch (Exception e) {
            System.err.println("获取数据库路径失败: " + e.getMessage());
            return "jdbc:sqlite:xiaohu.db";
        }
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
                // 如果是开发环境，返回当前工作目录
                return System.getProperty("user.dir");
            }
        } catch (Exception e) {
            // 出错时返回当前工作目录
            return System.getProperty("user.dir");
        }
    }

    /**
     * 根据多个条件模糊查询视频数据
     * @param name 名称关键词
     * @param code 编号关键词
     * @param performer 演员关键词
     * @param types 类型关键词
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 视频数据列表
     */
    public static List<VideoData> searchVideos(String name, String code, String performer, String types, int page, int pageSize) {
        List<VideoData> videos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM xiaohu_video");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        
        // 添加查询条件
        if (name != null && !name.trim().isEmpty()) {
            conditions.add("name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        
        if (code != null && !code.trim().isEmpty()) {
            conditions.add("code LIKE ?");
            params.add("%" + code.trim() + "%");
        }
        
        if (performer != null && !performer.trim().isEmpty()) {
            conditions.add("performer LIKE ?");
            params.add("%" + performer.trim() + "%");
        }
        
        if (types != null && !types.trim().isEmpty()) {
            conditions.add("types LIKE ?");
            params.add("%" + types.trim() + "%");
        }
        
        // 添加WHERE子句
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        // 按日期降序排序
        sql.append(" ORDER BY date DESC, id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    VideoData video = new VideoData();
                    video.setId(rs.getInt("id"));
                    video.setName(rs.getString("name"));
                    
                    // 读取日期字符串并转换为Date对象
                    String dateStr = rs.getString("date");
                    if (dateStr != null && !dateStr.trim().isEmpty()) {
                        try {
                            java.text.SimpleDateFormat dateSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            video.setDate(dateSdf.parse(dateStr));
                        } catch (java.text.ParseException e) {
                            // 如果解析失败，设置为null
                            video.setDate(null);
                        }
                    } else {
                        video.setDate(null);
                    }
                    
                    video.setCode(rs.getString("code"));
                    video.setUrl(rs.getString("url"));
                    video.setPreview(rs.getString("preview"));
                    video.setImg(rs.getString("img"));
                    video.setPerformer(rs.getString("performer"));
                    video.setDuration(rs.getString("duration"));
                    video.setTypes(rs.getString("types"));
                    
                    // 读取更新时间字符串并转换为Date对象
                    String updateTimeStr = rs.getString("update_time");
                    if (updateTimeStr != null && !updateTimeStr.trim().isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            video.setUpdateTime(sdf.parse(updateTimeStr));
                        } catch (java.text.ParseException e) {
                            // 如果解析失败，设置为null
                            video.setUpdateTime(null);
                        }
                    } else {
                        video.setUpdateTime(null);
                    }
                    
                    video.setRemark(rs.getString("remark"));
                    video.setFile(rs.getString("file"));
                    videos.add(video);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return videos;
    }

    /**
     * 获取总记录数
     * @param name 名称关键词
     * @param code 编号关键词
     * @param performer 演员关键词
     * @param types 类型关键词
     * @return 总记录数
     */
    public static int getTotalCount(String name, String code, String performer, String types) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM xiaohu_video");
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        
        // 添加查询条件
        if (name != null && !name.trim().isEmpty()) {
            conditions.add("name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        
        if (code != null && !code.trim().isEmpty()) {
            conditions.add("code LIKE ?");
            params.add("%" + code.trim() + "%");
        }
        
        if (performer != null && !performer.trim().isEmpty()) {
            conditions.add("performer LIKE ?");
            params.add("%" + performer.trim() + "%");
        }
        
        if (types != null && !types.trim().isEmpty()) {
            conditions.add("types LIKE ?");
            params.add("%" + types.trim() + "%");
        }
        
        // 添加WHERE子句
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除视频数据
     * @param id 视频ID
     * @return 是否删除成功
     */
    public static boolean deleteVideo(Integer id) {
        String sql = "DELETE FROM xiaohu_video WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 新增视频数据
     * @param video 视频数据
     * @return 是否新增成功
     */
    public static boolean addVideo(VideoData video) {
        String sql = "INSERT INTO xiaohu_video (name, date, code, url, preview, img, performer, duration, types, update_time, remark, file) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, video.getName());
            
            // 使用格式化字符串设置日期
            java.text.SimpleDateFormat dateSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateStr = video.getDate() != null ? dateSdf.format(video.getDate()) : null;
            pstmt.setString(2, dateStr);
            
            pstmt.setString(3, video.getCode());
            pstmt.setString(4, video.getUrl());
            pstmt.setString(5, video.getPreview());
            pstmt.setString(6, video.getImg());
            pstmt.setString(7, video.getPerformer());
            pstmt.setString(8, video.getDuration());
            pstmt.setString(9, video.getTypes());
            
            
            String updateTimeStr = video.getUpdateTime() != null ? dateSdf.format(video.getUpdateTime()) : dateSdf.format(new java.util.Date());
            pstmt.setString(10, updateTimeStr);
            
            pstmt.setString(11, video.getRemark());
            pstmt.setString(12, video.getFile());
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新视频数据
     * @param video 视频数据
     * @return 是否更新成功
     */
    public static boolean updateVideo(VideoData video) {
        String sql = "UPDATE xiaohu_video SET name=?, date=?, code=?, url=?, preview=?, img=?, performer=?, duration=?, types=?, update_time=?, remark=?, file=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, video.getName());
            
            // 使用格式化字符串设置日期
            java.text.SimpleDateFormat dateSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateStr = video.getDate() != null ? dateSdf.format(video.getDate()) : null;
            pstmt.setString(2, dateStr);
            
            pstmt.setString(3, video.getCode());
            pstmt.setString(4, video.getUrl());
            pstmt.setString(5, video.getPreview());
            pstmt.setString(6, video.getImg());
            pstmt.setString(7, video.getPerformer());
            pstmt.setString(8, video.getDuration());
            pstmt.setString(9, video.getTypes());
            
            String updateTimeStr = dateSdf.format(new java.util.Date());
            pstmt.setString(10, updateTimeStr);
            
            pstmt.setString(11, video.getRemark());
            pstmt.setString(12, video.getFile());
            pstmt.setInt(13, video.getId());
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 