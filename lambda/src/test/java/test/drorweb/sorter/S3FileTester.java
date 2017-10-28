package test.drorweb.sorter;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.gson.Gson;
import org.drorweb.sorter.file.MyFile;
import org.drorweb.sorter.lambda.FilesToMergeMessage;

import java.io.*;
import java.util.List;


public class S3FileTester {

    private AWSCredentials creds;
    private String bucketName, keyName;
    private MyFile file;

    public static void main(String[] args) {

        try {
            String s = "{filesToMerge=[{eof=false, bucketName=algosec, keyName=file-to-sort_tmp/3afb8a29-83d4-4e7b-b6b8-83e9d3078691}, {eof=false, bucketName=algosec, keyName=file-to-sort_tmp/1c1e047a-e79b-47ff-bdcf-50a5d20e4c91}]}";
            Gson gson = new Gson();
            FilesToMergeMessage filesToMergeMessage = gson.fromJson(s, FilesToMergeMessage.class);
//            test();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void test() throws Exception {
        MyConfig config = MyConfig.getInstance();
        BasicAWSCredentials creds = new BasicAWSCredentials(config.getAccessKeyId(), config.getSecretAccessKey());
        String bucketName="algosec";
//        String keyName="input.txt";
//        String keyName="test-generate-file-AG";
//            String keyName="file-to-sort/tmp/96bed698-275f-491f-af94-806bd85d17af";
        String keyName="file-to-sort";
        S3FileTester gen = new S3FileTester(creds, bucketName, keyName);

        //            gen.generate(1000000);
//            gen.printRange(0,99);
        gen.printRange(0,99);
//            System.out.println(gen.getFile().size());

    }

    private void printRange(long startPointer, long endPointer) throws IOException {
        List<Double> range = file.readRange(startPointer, endPointer);
        for (Double aDouble : range) {
            System.out.println(aDouble);
        }
    }

    public S3FileTester(AWSCredentials creds, String bucketName, String keyName) {
        this.creds = creds;
        this.bucketName = bucketName;
        this.keyName = keyName;
        file = new MyFile(creds, bucketName, keyName);
    }

    public void generate(final long size) throws Throwable {
        file.generateRandom(size);
    }

    private MyFile getFile() {
        return file;
    }

}
