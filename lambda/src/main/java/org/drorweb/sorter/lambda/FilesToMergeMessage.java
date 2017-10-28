package org.drorweb.sorter.lambda;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class FilesToMergeMessage {

    private List<FileToMergeMessage> filesToMerge;

    public FilesToMergeMessage() {
        filesToMerge = new ArrayList<>();
    }

    public void add(FileToMergeMessage ftm) {
        filesToMerge.add(ftm);
    }

    public List<FileToMergeMessage> getFilesToMerge() {
        return filesToMerge;
    }

    public String toString() {
        return toMessage();
    }

    public String toMessage() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
