package org.drorweb.sorter.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.io.EOFException;
import java.util.List;
import java.util.UUID;

public class MyQueue {

    private AmazonSQS sqs;
    private String name;
    private String url;

    public  MyQueue(String name) {
        this.name = name;
        sqs = AmazonSQSClientBuilder.defaultClient();
        this.url = sqs.getQueueUrl(name).getQueueUrl();
    }

    public void sendMessage(String body) {

        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(url)
                .withMessageBody(body)
                .withMessageGroupId("file-sorter")
                .withMessageDeduplicationId(UUID.randomUUID().toString())
                ;
        sqs.sendMessage(send_msg_request);
    }

    public Message readMessage() throws EOFException {
        ReceiveMessageResult result = sqs.receiveMessage(new ReceiveMessageRequest(url)
                .withMaxNumberOfMessages(1));

        List<Message> msgs = result.getMessages();
        if (msgs != null && msgs.size() > 0) {
            return msgs.get(0);
        } else {
            throw new EOFException();
        }

    }

}
