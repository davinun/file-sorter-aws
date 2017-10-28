package org.drorweb.sorter.lambda;

import com.google.gson.Gson;

public class FileToMergeMessage {

    private boolean eof;
    private String bucketName;
    private String keyName;

    public FileToMergeMessage(boolean eof) {
        this.eof = eof;
    }

    public FileToMergeMessage(String bucketName, String keyName) {
        this.bucketName = bucketName;
        this.keyName = keyName;
    }

    public boolean isEof() {
        return eof;
    }

    private void setEof(boolean eof) {
        this.eof = eof;
    }

    public String getBucketName() {
        return bucketName;
    }

    private void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKeyName() {
        return keyName;
    }

    private void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String toString() {
        return toMessage();
    }

    public String toMessage() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static FileToMergeMessage fromMessage(String msg) {
        Gson gson = new Gson();
        return gson.fromJson(msg, FileToMergeMessage.class);
    }
}
