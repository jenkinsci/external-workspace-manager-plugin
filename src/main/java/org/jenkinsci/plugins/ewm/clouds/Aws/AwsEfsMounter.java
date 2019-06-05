package org.jenkinsci.plugins.ewm.clouds.Aws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.elasticfilesystem.AmazonElasticFileSystem;
import com.amazonaws.services.elasticfilesystem.AmazonElasticFileSystemClient;
import com.amazonaws.services.elasticfilesystem.AmazonElasticFileSystemClientBuilder;
import com.amazonaws.services.elasticfilesystem.model.CreateFileSystemRequest;
import com.amazonaws.services.elasticfilesystem.model.CreateFileSystemResult;
import com.amazonaws.services.elasticfilesystem.model.CreateMountTargetRequest;
import org.jenkinsci.plugins.ewm.definitions.AwsEfsDisk;
import org.jenkinsci.plugins.ewm.definitions.Disk;
import hudson.model.Node;

public  class AwsEfsMounter {

    public static void doMountPreparation(AwsEfsDisk disk) {
        String fileSystemId = doCreateElasticFileSystem(disk);
        doCreateMountTarget(disk, fileSystemId);
    }
    public static void doMountElasticFileSystem() {

    }


    public static String doCreateElasticFileSystem(AwsEfsDisk awsEfsDisk) {
        // TODO : what is the proper way to cast awsEfsDisk to AwsEfsDisk type
        // TODO : should I design a factory out of this ?
        AmazonElasticFileSystem elasticFileSystem = AmazonElasticFileSystemClientBuilder.standard()
                .withRegion(awsEfsDisk.getRegion())
                .withCredentials(new ProfileCredentialsProvider("dafault"))
                .build();
        CreateFileSystemRequest createFileSystemRequest = new CreateFileSystemRequest()
                .withCreationToken("dummy")
                .withEncrypted(false)
                .withKmsKeyId("dummy")
                .withPerformanceMode("dummy")
                .withThroughputMode("dummy");

        // TODO : can put this client to a static class

        // TODO : is the this profile provider using IAM role profile ?

        CreateFileSystemResult createFileSystemResult = elasticFileSystem.createFileSystem(createFileSystemRequest);
        return createFileSystemResult.getFileSystemId();
    }


    public static void doCreateMountTarget(AwsEfsDisk awsEfsDisk, String fileSystemId) {
        // TODO : current use default mount target settings
        CreateMountTargetRequest createMountTargetRequest = new CreateMountTargetRequest()
                .withFileSystemId(fileSystemId);


    }

    public static void doCreateTags(AwsEfsDisk awsEfsDisk) {

    }


}
