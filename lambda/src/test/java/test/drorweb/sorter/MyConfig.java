package test.drorweb.sorter;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class MyConfig {

    private String accessKeyId;
    private String secretAccessKey;

    private static MyConfig instance;

    public static void main(String[] args) {

        try {
            MyConfig cnf = MyConfig.getInstance();
            System.out.println(cnf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static MyConfig getInstance() throws FileNotFoundException {
        if (instance == null) {
            ClassLoader classLoader = MyConfig.class.getClassLoader();
            File file = new File(classLoader.getResource("config.json").getFile());
            Gson gson = new Gson();
            instance = gson.fromJson(new FileReader(file), MyConfig.class);
        }
        return instance;
    }

    public MyConfig(String accessKeyId, String secretAccessKey) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    @Override
    public String toString() {
        return "MyConfig{" +
                "accessKeyId='" + accessKeyId + '\'' +
                ", secretAccessKey='" + secretAccessKey + '\'' +
                '}';
    }
}
