package org.drorweb.sorter.lambda;

import com.google.gson.Gson;

public class StartMergeConsumerMessage {

    private String bucketName;
    private String InputKeyName;
    private String outputKeyName;
    private String queueName;

    public StartMergeConsumerMessage(String bucketName, String inputKeyName, String outputKeyName, String queueName) {
        this.bucketName = bucketName;
        InputKeyName = inputKeyName;
        this.outputKeyName = outputKeyName;
        this.queueName = queueName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getInputKeyName() {
        return InputKeyName;
    }

    public String getOutputKeyName() {
        return outputKeyName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String toString() {
        return toMessage();
    }

    public String toMessage() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
