package dev.openfeature.contrib.providers.flagd.resolver.process.storage;

import dev.openfeature.contrib.providers.flagd.resolver.process.model.FeatureFlag;

import java.util.concurrent.BlockingQueue;

public interface Storage {
    void init() ;

    void shutdown();
    
    FeatureFlag getFLag(final String key) ;

    BlockingQueue<StorageState> getStateQueue();
}
