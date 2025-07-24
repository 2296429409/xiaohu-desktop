package com.xiaohu.fileupload.pojo;

import java.util.Date;

/**
 * 视频数据模型类
 * 对应数据库表 xiaohu_video
 */
public class VideoData {
    private Integer id;
    private String name;
    private Date date;
    private String code;
    private String url;
    private String preview;
    private String img;
    private String performer;
    private String duration;
    private String types;
    private Date updateTime;
    private String remark;
    private String file;

    // 构造函数
    public VideoData() {}

    public VideoData(Integer id, String name, Date date, String code, String url, 
                    String preview, String img, String performer, String duration, 
                    String types, Date updateTime, String remark, String file) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.code = code;
        this.url = url;
        this.preview = preview;
        this.img = img;
        this.performer = performer;
        this.duration = duration;
        this.types = types;
        this.updateTime = updateTime;
        this.remark = remark;
        this.file = file;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "VideoData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", code='" + code + '\'' +
                ", url='" + url + '\'' +
                ", preview='" + preview + '\'' +
                ", img='" + img + '\'' +
                ", performer='" + performer + '\'' +
                ", duration='" + duration + '\'' +
                ", types='" + types + '\'' +
                ", updateTime=" + updateTime +
                ", remark='" + remark + '\'' +
                ", file='" + file + '\'' +
                '}';
    }
} 