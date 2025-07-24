package com.xiaohu.fileupload.pojo;

/**
 * ts上传结果
 * @author xiaxh
 * @date 2025/7/15
 */
public class TsUploadResult {
    private String key;
    private Long fileId;

    public TsUploadResult(String key, Long fileId) {
        this.key = key;
        this.fileId = fileId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
