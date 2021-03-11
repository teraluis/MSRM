package services;

import com.typesafe.config.Config;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

import javax.inject.Inject;
import java.io.InputStream;

public class FileService {

    private String url;
    private String accessKey;
    private String secretKey;
    private MinioClient minioClient;

    @Inject
    public FileService(final Config config) {

        this.url = config.getString("minio.url");
        this.accessKey = config.getString("minio.accesskey");
        this.secretKey = config.getString("minio.secretkey");

        try {
            this.minioClient = new MinioClient(url, accessKey, secretKey);
        } catch (InvalidEndpointException | InvalidPortException e) {
            e.printStackTrace();
        }
    }

    public void upload(String organization, String uuid, String collection, InputStream inputStream, String filename, long fileSize, String contentType) {
        this.initBucket(organization);
        try {
            minioClient.putObject(this.getBucketName(organization), "crm/" + collection + "/" + uuid + "/" + filename, inputStream, fileSize, null, null, contentType);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream download(String organization, String uuid, String collection, String fileName) {
        this.initBucket(organization);

        try {
            return minioClient.getObject(this.getBucketName(organization), "crm/" + collection + "/" + uuid + "/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public ObjectStat getStatObject(String organization, String uuid, String collection, String fileName) {
        this.initBucket(organization);

        try {
            return minioClient.statObject(this.getBucketName(organization), "crm/" + collection + "/" + uuid + "/" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void remove(String organization, String uuid, String collection, String filename) {
        this.initBucket(organization);
        try {
            minioClient.removeObject(this.getBucketName(organization), "crm/" + collection + "/" + uuid + "/" + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBucket(String organization) {
        String bucketName = this.getBucketName(organization);
        try {
            if (!minioClient.bucketExists(bucketName)) {
                minioClient.makeBucket(bucketName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBucketName(String organization) {
        return organization.toLowerCase() + "-bucket";
    }
}
