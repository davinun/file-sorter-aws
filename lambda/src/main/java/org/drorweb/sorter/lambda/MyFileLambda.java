package org.drorweb.sorter.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.google.gson.Gson;
import org.drorweb.sorter.file.MyFile;
import org.drorweb.sorter.notifications.MyNotifications;
import org.drorweb.sorter.queue.MyQueue;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MyFileLambda {

    private static final String firstPassQueueName = "sorter-phase-1.fifo";
    private static final String firstPassSnsTopicArn = "arn:aws:sns:us-east-1:218073620992:file-sorter-phase-1";

//    private static int pageSize = 30000000; //30,000,000doubles ==> 300MB Page size
    private static int pageSize = 300000; //300,000doubles ==> 3MB Page size

    public FileGenerateResponse generate(final FileGenerateRequest request, final Context context) throws Throwable {
        try {
            try {
                MyFile file = new MyFile(request.getBucketName(), request.getKeyName());
                file.generateRandom(request.getSize());
            } catch (Throwable e) {
                e.printStackTrace();
                context.getLogger().log("Error | MyFileLambda | generate | "+e);
            }

            return new FileGenerateResponse("File generation running in background. Request:"+request);
        } catch (Throwable e) {
            context.getLogger().log("Error | MyFileLambda | generate | "+e);
            throw e;
        }
    }

    public FileSortResponse sort(final FileSortRequest request, final Context context) throws Exception {
        try {
            MyFile file = new MyFile(request.getBucketName(), request.getKeyName());
            long fileSize = file.size();

//            MyQueue queue = new MyQueue(firstPassQueueName);
            MyNotifications notifs = new MyNotifications();

            for (long i=0; i < fileSize; i+=pageSize) {
                long startRange = i * MyFile.numberSizeInBytes;
                long endRange = startRange + (pageSize * MyFile.numberSizeInBytes) - 1;
                FirstPassMessage msg = new FirstPassMessage(startRange, endRange, request.getBucketName(), request.getKeyName());

//                queue.sendMessage(msg.toMessage());
                notifs.send(firstPassSnsTopicArn, msg.toMessage());
            }

            return new FileSortResponse("Sort is running in background. Request:"+request);
        } catch (Exception e) {
            context.getLogger().log("Error | MyFileLambda | sort | "+e);
            throw e;
        }
    }

    public void firstPass(SNSEvent event, final Context context) throws Throwable {
        String msgStr = event.getRecords().get(0).getSNS().getMessage();
        context.getLogger().log("msgStr.toString():"+msgStr.toString());
        try {
            Gson gson = new Gson();
            FirstPassMessage msg = gson.fromJson(msgStr.toString(), FirstPassMessage.class);
            MyFile file = new MyFile(msg.getBucketName(), msg.getKeyName());
            List<Double> page = file.readRange(msg.getStartRange(), msg.getEndRange());
            page.sort(Double::compare);

            MyFile output = new MyFile(msg.getBucketName(), msg.getKeyName()+"/tmp/"+ UUID.randomUUID().toString());
            output.write(page);
        } catch (Throwable e) {
            e.printStackTrace();
            context.getLogger().log("Error | MyFileLambda | firstPass | "+e);
            throw e;
        }
    }
}
