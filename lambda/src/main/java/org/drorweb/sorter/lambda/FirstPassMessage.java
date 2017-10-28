package org.drorweb.sorter.lambda;

import com.google.gson.Gson;

public class FirstPassMessage {
    private long startRange;
    private long endRange;
    private String bucketName;
    private String keyName;

    public FirstPassMessage(long startRange, long endRange, String bucketName, String keyName) {
        this.startRange = startRange;
        this.endRange = endRange;
        this.bucketName = bucketName;
        this.keyName = keyName;
    }

    public long getStartRange() {
        return startRange;
    }

    public long getEndRange() {
        return endRange;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getKeyName() {
        return keyName;
    }

    public String toMessage() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
