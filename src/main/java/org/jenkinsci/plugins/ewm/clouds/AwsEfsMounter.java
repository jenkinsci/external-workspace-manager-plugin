package org.jenkinsci.plugins.ewm.clouds;

import com.amazonaws.services.elasticfilesystem.AmazonElasticFileSystemClientBuilder;
import com.amazonaws.services.elasticfilesystem.model.CreateFileSystemRequest;

public static class AwsEfsMounter {
    public static void createEfs() {
        // TODO, how should I place the AwsElasticFileSystemClient, in a singleton ?
        CreateFileSystemRequest createFileSystemRequest = new CreateFileSystemRequest();
        AmazonElasticFileSystemClientBuilder amazonElasticFileSystemClientBuilder = new AmazonElasticFileSystemClientBuilder();


    }


}
