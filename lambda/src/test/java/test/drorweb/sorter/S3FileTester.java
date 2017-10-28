package test.drorweb.sorter;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.drorweb.sorter.file.MyFile;

import java.io.*;
import java.util.List;


public class S3FileTester {

    private AWSCredentials creds;
    private String bucketName, keyName;
    private MyFile file;

    public static void main(String[] args) {

        BasicAWSCredentials creds = new BasicAWSCredentials(MyAWSCredentials.accessKeyId, MyAWSCredentials.secretAccessKey);
        String bucketName="algosec";
//        String keyName="input.txt";
//        String keyName="test-generate-file-AG";
        String keyName="file-to-sort/tmp/96bed698-275f-491f-af94-806bd85d17af";
//        String keyName="file-to-sort";
        S3FileTester gen = new S3FileTester(creds, bucketName, keyName);
        try {
//            gen.generate(1000000);
//            gen.printRange(0,99);
            gen.printRange(0,99);
//            System.out.println(gen.getFile().size());
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
