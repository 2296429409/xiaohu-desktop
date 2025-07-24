package com.xiaohu.fileupload.pojo;

/**
 * @author xiaxh
 * @date 2025/7/15
 */
public class UploadResult {
    private String fileName;
    private String key;
    private String error;
    private Long fileId;

    public UploadResult(String fileName, String key, String error, Long fileId) {
        this.fileName = fileName;
        this.key = key;
        this.error = error;
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
