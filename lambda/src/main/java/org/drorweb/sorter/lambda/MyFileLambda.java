package org.drorweb.sorter.lambda;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import org.drorweb.sorter.file.MyFile;
import org.drorweb.sorter.notifications.MyNotifications;
import org.drorweb.sorter.queue.MyQueue;

import java.util.List;
import java.util.UUID;

public class MyFileLambda {

    private static final String mergeLambdaName = "sorter-file-merge"; //merge
    private static final String mergeConsumerLambdaName = "sorter-file-merge-consumer"; //mergeConsumer
    private static final String fileToMergeQueueName = "sorter-file-merge-queue.fifo";
    private static final String firstPassSnsTopicArn = "arn:aws:sns:us-east-1:218073620992:file-sorter-phase-1";
    private static final String mergeConsumerSnsTopicArn = "arn:aws:sns:us-east-1:218073620992:file-sorter-merge";
    private static final int bufferSize = 2;

    //    private static int pageSize = 30000000; //30,000,000doubles ==> 300MB Page size
    private static int pageSize = 300000; //300,000doubles ==> 3MB Page size

    public FileGenerateResponse generate(final FileGenerateRequest request, final Context context) throws Throwable {
        try {
            try {
                MyFile file = new MyFile(request.getBucketName(), request.getKeyName());
                file.generateRandom(request.getSize());
            } catch (Throwable e) {
                e.printStackTrace();
                context.getLogger().log("Error | MyFileLambda | generate | " + e);
            }

            return new FileGenerateResponse("File generation running in background. Request:" + request);
        } catch (Throwable e) {
            context.getLogger().log("Error | MyFileLambda | generate | " + e);
            throw e;
        }
    }

    public FileSortResponse sort(final FileSortRequest request, final Context context) throws Exception {
        try {
            MyFile file = new MyFile(request.getBucketName(), request.getKeyName());
            long fileSize = file.size();

            MyNotifications notifs = new MyNotifications();

            for (long i = 0; i < fileSize; i += pageSize) {
                long startRange = i * MyFile.numberSizeInBytes;
                long endRange = startRange + (pageSize * MyFile.numberSizeInBytes) - 1;
                FirstPassMessage msg = new FirstPassMessage(startRange, endRange, request.getBucketName(), request.getKeyName());

                notifs.send(firstPassSnsTopicArn, msg.toMessage());
            }

            //Invoke the merge consumer Lambda
            String outputFileName = request.getKeyName() + "_SORTED";
            StartMergeConsumerMessage smcMsg
                    = new StartMergeConsumerMessage(request.getBucketName(),
                    request.getKeyName(),
                    outputFileName,
                    fileToMergeQueueName);

            AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
            InvokeRequest lambdaRequest = new InvokeRequest();
            lambdaRequest.setInvocationType(InvocationType.Event);
            lambdaRequest.setFunctionName(mergeConsumerLambdaName);
            lambdaRequest.setPayload(smcMsg.toMessage());
            lambdaClient.invoke(lambdaRequest);

            return new FileSortResponse("Sort is running in background. Request:" + request);
        } catch (Exception e) {
            context.getLogger().log("Error | MyFileLambda | sort | " + e);
            throw e;
        }
    }

    public void firstPass(SNSEvent event, final Context context) throws Throwable {
        String msgStr = event.getRecords().get(0).getSNS().getMessage();
        context.getLogger().log("msgStr.toString():" + msgStr.toString());
        try {
            Gson gson = new Gson();
            FirstPassMessage msg = gson.fromJson(msgStr.toString(), FirstPassMessage.class);
            MyFile file = new MyFile(msg.getBucketName(), msg.getKeyName());
            List<Double> page = file.readRange(msg.getStartRange(), msg.getEndRange());
            page.sort(Double::compare);

            String tmpFileName = msg.getKeyName() + "_tmp/" + UUID.randomUUID().toString();
            MyFile output = new MyFile(msg.getBucketName(), tmpFileName);
            output.write(page);

            MyQueue queue = new MyQueue(fileToMergeQueueName);
            FileToMergeMessage ftmMsg = new FileToMergeMessage(msg.getBucketName(), tmpFileName);
            queue.sendMessage(ftmMsg.toMessage());

        } catch (Throwable e) {
            e.printStackTrace();
            context.getLogger().log("Error | MyFileLambda | firstPass | " + e);
            throw e;
        }
    }

    /**
     *
     * Loop --> read two files to merge from the queue and invoke the MERGE lambda to merge them
     * End-of-Loop --> rename the final file and exit
     *
     * @param msgObj
     * @param context
     */
    public void mergeConsumer(Object msgObj, final Context context) throws Exception {
        try {
            Gson gson = new Gson();
            StartMergeConsumerMessage message = gson.fromJson(msgObj.toString(), StartMergeConsumerMessage.class);
            context.getLogger().log("Debug | MyFileLambda | mergeConsumer | started " + message);
            MyQueue queue = new MyQueue(message.getQueueName());

            List<Message> msgs = queue.read();

            FilesToMergeMessage filesToMergeMessage = new FilesToMergeMessage();
            boolean cont = true;
            while (cont) {
                context.getLogger().log("Debug | MyFileLambda | mergeConsumer | read " + msgs.size() + " messages");

                for (Message msg : msgs) {

                    context.getLogger().log("Debug | MyFileLambda | mergeConsumer | message: " + msg.getBody());
                    FileToMergeMessage ftmMsg = FileToMergeMessage.fromMessage(msg.getBody());
                    if (ftmMsg.isEof()) {
                        //todo: handleEndOfSort();
                        cont = false;
                        break;

                    } else {

                        filesToMergeMessage.add(ftmMsg);

                        if (filesToMergeMessage.getFilesToMerge().size() == bufferSize) {
                            AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
                            InvokeRequest lambdaRequest = new InvokeRequest();
                            lambdaRequest.setInvocationType(InvocationType.Event);
                            lambdaRequest.setFunctionName(mergeLambdaName);
                            lambdaRequest.setPayload(filesToMergeMessage.toMessage());
                            lambdaClient.invoke(lambdaRequest);
                            context.getLogger().log("Debug | MyFileLambda | mergeConsumer | sent " + filesToMergeMessage.toMessage());

                            filesToMergeMessage = new FilesToMergeMessage();
                        }
                    }
                }

                try {
                    wait(2000); //wait 2 sec
                } catch (Exception e) {
                    //ignore
                }

                msgs = queue.read();
            }

            if (filesToMergeMessage.getFilesToMerge().size() > 1) {
                AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
                InvokeRequest lambdaRequest = new InvokeRequest();
                lambdaRequest.setInvocationType(InvocationType.Event);
                lambdaRequest.setFunctionName(mergeLambdaName);
                lambdaRequest.setPayload(filesToMergeMessage.toMessage());
                lambdaClient.invoke(lambdaRequest);
                context.getLogger().log("Debug | MyFileLambda | mergeConsumer | sent " + filesToMergeMessage.toMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log("Error | MyFileLambda | mergeConsumer | " + e);
            throw e;
        } finally {
            context.getLogger().log("Info | MyFileLambda | mergeConsumer | finished");

        }


    }

    public void merge(Object msgObj, final Context context) {
        context.getLogger().log("Info | MyFileLambda | merge | started: "+msgObj.toString());

        Gson gson = new Gson();
        FilesToMergeMessage filesToMergeMessage = gson.fromJson(msgObj.toString(), FilesToMergeMessage.class);
        //todo...

        for (FileToMergeMessage fileToMergeMessage : filesToMergeMessage.getFilesToMerge()) {
            context.getLogger().log("Info | MyFileLambda | merge | file: "+fileToMergeMessage);
        }

        MyQueue queue = new MyQueue(fileToMergeQueueName);
        FileToMergeMessage ftmMsg = new FileToMergeMessage(true);//todo--- remove
        queue.sendMessage(ftmMsg.toMessage());

        context.getLogger().log("Info | MyFileLambda | merge | finished");


    }
}