package org.drorweb.sorter.notifications;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;

public class MyNotifications {

    private AmazonSNS snsClient;

    public MyNotifications() {
        snsClient = AmazonSNSClientBuilder.defaultClient();
    }

    public void send(String topicArn, String msg) {
        snsClient.publish(new PublishRequest(topicArn, msg));
    }
}
