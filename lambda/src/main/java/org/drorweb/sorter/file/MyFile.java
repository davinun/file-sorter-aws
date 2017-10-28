package org.drorweb.sorter.file;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.drorweb.sorter.utils.ThreadExceptionHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A S3 binary file with a list of comma separated doubles.
 * Use DataOutputStream to stream "double,double,double,..."
 */
public class MyFile {

    private AWSCredentials creds;
    private String bucketName, keyName;
    private AmazonS3 s3Client;

    public static final long numberSizeInBytes = 10; //double=8 bytes //char=2 bytes

    public MyFile(String bucketName, String keyName) {
        this.creds = null;
        this.bucketName = bucketName;
        this.keyName = keyName;

        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

    }

    public MyFile(AWSCredentials creds, String bucketName, String keyName) {
        this.creds = creds;
        this.bucketName = bucketName;
        this.keyName = keyName;

        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();

    }

    /**
     * Writes the data input stream to the S3 Object
     *
     * @param in - a DataInputStream in the format double,double,double,...
     * @param size - number of numbers in the stream
     */
    private void write(InputStream in, long size) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size * numberSizeInBytes);

        s3Client.putObject(new PutObjectRequest(bucketName, keyName, in, metadata));

    }

    /**
     * Print the file to the System.out
     */
    public void print() {

        GetObjectRequest request = new GetObjectRequest(bucketName, keyName);
        S3Object s3obj = s3Client.getObject(request);
        S3ObjectInputStream in = s3obj.getObjectContent();
        DataInputStream dis = new DataInputStream(in);
        try {
            while(true) {
                System.out.println(dis.readDouble());
                dis.readChar();
            }
        } catch (EOFException e) {
            System.out.println("Finished reading the file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Read a range of numbers from the file
     *
     * @param startPointer
     * @param endPointer
     * @return a list of doubles from the file
     * @throws IOException
     */
    public List<Double> readRange(long startPointer, long endPointer) throws IOException {

        List<Double> response = new ArrayList<>();

        GetObjectRequest request = new GetObjectRequest(bucketName, keyName);
        request.setRange(startPointer, endPointer);

        S3Object objectPortion = s3Client.getObject(request);

        InputStream objectData = objectPortion.getObjectContent();

        try {
            DataInputStream dis = new DataInputStream(objectData);

            while(true) {
                response.add(dis.readDouble());
                dis.readChar();
            }

        } catch (EOFException e) {
            System.out.println("Finished reading the part");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     *
     * @return the number of doubles in the file
     */
    public long size() {
        ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, keyName);
        return metadata.getContentLength() / numberSizeInBytes;
    }

    public void generateRandom(final long size) throws Throwable {

        PipedInputStream in = new PipedInputStream();
        final DataOutputStream out = new DataOutputStream(new PipedOutputStream(in));
        Thread t1 = new Thread(() -> {
            try {
                for (int i=0; i < size; i++) {
                    double num = 1000000 * Math.random();
                    out.writeDouble(num);
                    out.writeChar(',');
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        ThreadExceptionHandler exHandler = new ThreadExceptionHandler();
        t1.setUncaughtExceptionHandler(exHandler);
        t1.start();

        write(in, size);

        t1.join();
        if (exHandler.getException() != null) {
            throw exHandler.getException();
        }

    }

    public void write(List<Double> doubles) throws Throwable {
        PipedInputStream in = new PipedInputStream();
        final DataOutputStream out = new DataOutputStream(new PipedOutputStream(in));
        Thread t1 = new Thread(() -> {
            try {
                for (Double num : doubles) {
                    out.writeDouble(num);
                    out.writeChar(',');
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        ThreadExceptionHandler exHandler = new ThreadExceptionHandler();
        t1.setUncaughtExceptionHandler(exHandler);
        t1.start();

        write(in, doubles.size());

        t1.join();
        if (exHandler.getException() != null) {
            throw exHandler.getException();
        }

    }

}

