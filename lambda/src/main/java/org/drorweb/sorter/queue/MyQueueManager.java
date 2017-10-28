package org.drorweb.sorter.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;

import java.util.List;

public class MyQueueManager {

    private AmazonSQS sqs;

    public MyQueueManager() {
        this.sqs = AmazonSQSClientBuilder.defaultClient();
    }

    /**
     * get a list of all queue URLs starting with the prefix
     *
     * @param prefix
     * @return
     */
    public List<String> listQueues(String prefix) {
        ListQueuesRequest request = new ListQueuesRequest()
                .withQueueNamePrefix(prefix);

        ListQueuesResult result = sqs.listQueues(request);

        return result.getQueueUrls();
    }

    public String getQueueUrlByName(String name) {
        return sqs.getQueueUrl(name).getQueueUrl();
    }


}
