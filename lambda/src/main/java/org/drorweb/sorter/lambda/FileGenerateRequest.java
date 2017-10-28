package org.drorweb.sorter.lambda;

public class FileGenerateRequest {
    private String bucketName;
    private String keyName;
    private int size;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "FileGenerateRequest{" +
                "bucketName='" + bucketName + '\'' +
                ", keyName='" + keyName + '\'' +
                ", size=" + size +
                '}';
    }
}
