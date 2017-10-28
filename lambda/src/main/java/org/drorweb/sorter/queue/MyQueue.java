package org.drorweb.sorter.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.io.EOFException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MyQueue {

    private AmazonSQS sqs;
    private String name;
    private String url;

/*
    public static MyQueue createFifo(String name) {
        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        Map<String, String> attributes = new HashMap<String, String>();
        // A FIFO queue must have the FifoQueue attribute set to True
        attributes.put("FifoQueue", "true");
        // Generate a MessageDeduplicationId based on the content, if the user doesn't provide a MessageDeduplicationId
        attributes.put("ContentBasedDeduplication", "true");
        // The FIFO queue name must end with the .fifo suffix

        CreateQueueResult resp = sqs.createQueue(new CreateQueueRequest()
                .withQueueName(name)
                .withAttributes(attributes));
        return new MyQueue(name);
    }
*/

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

    public List<Message> read() {
        ReceiveMessageResult result = sqs.receiveMessage(new ReceiveMessageRequest(url)
                .withWaitTimeSeconds(20)
                .withMaxNumberOfMessages(10));

        List<Message> msgs = result.getMessages();
        return msgs;
    }

}
