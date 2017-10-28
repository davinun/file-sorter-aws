package org.drorweb.sorter.utils;

public class ThreadExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread thread;
    private Throwable exception;

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        this.thread = t;
        this.exception = e;
    }

    public Thread getThread() {
        return thread;
    }

    public Throwable getException() {
        return exception;
    }
}
