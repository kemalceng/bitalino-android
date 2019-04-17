package com.acoustic.bitalino;

public class TestInfo {
    private String wsId;
    private String fileId;
    private String test;

    public TestInfo(String wsId, String fileId, String test) {
        this.wsId = wsId;
        this.fileId = fileId;
        this.test = test;
    }

    public String getWsId() {
        return wsId;
    }

    public void setWsId(String wsId) {
        this.wsId = wsId;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
