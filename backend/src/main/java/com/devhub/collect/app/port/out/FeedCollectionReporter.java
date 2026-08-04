package com.devhub.collect.app.port.out;

public interface FeedCollectionReporter {

    Timing started();

    void collected(String feed, int stored);

    void unchanged(String feed);

    void failed(String feed);

    void errored(String feed);

    @FunctionalInterface
    interface Timing extends AutoCloseable {

        @Override
        void close();
    }
}