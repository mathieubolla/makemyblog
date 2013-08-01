import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.StringInputStream;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StorageService {
    private final AmazonS3 storage;
    private final String bucket;

    public StorageService(AmazonS3 storage, String bucket) {
        this.storage = storage;
        this.bucket = bucket;
    }

    public void sendPrivate(File inputFile, String key, String contentType) {
        try {
            send(key, contentType, CannedAccessControlList.BucketOwnerFullControl, new FileInputStream(inputFile), inputFile.length());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPublic(File inputFile, String key, String contentType) {
        try {
            send(key, contentType, CannedAccessControlList.PublicRead, new FileInputStream(inputFile), inputFile.length());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPublic(String content, String key, String contentType) {
        try {
            send(key, contentType, CannedAccessControlList.PublicRead, new StringInputStream(content), content.length());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPublic(byte[] content, String key, String contentType) {
       send(key, contentType, CannedAccessControlList.PublicRead, new ByteArrayInputStream(content), content.length);
    }

    private void send(String key, String contentType, CannedAccessControlList acl, InputStream inputStream, long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        if (contentType != null) {
            metadata.setContentType(contentType);
        }
        PutObjectRequest putObjectRequest;
        putObjectRequest = new PutObjectRequest(bucket, key, inputStream, metadata).withCannedAcl(acl);
        storage.putObject(putObjectRequest);
    }

    public boolean exists(String key) {
        try {
            storage.getObjectMetadata(new GetObjectMetadataRequest(bucket, key));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean exists(String key, File file) {
        try {
            ObjectMetadata metadata = storage.getObjectMetadata(new GetObjectMetadataRequest(bucket, key));

            return metadata.getETag().equals(md5(file));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exists(String key, byte[] data) {
        try {
            ObjectMetadata metadata = storage.getObjectMetadata(new GetObjectMetadataRequest(bucket, key));

            return metadata.getETag().equals(md5(new ByteArrayInputStream(data)));
        } catch (Exception e) {
            return false;
        }
    }

    private static String md5(File file) throws NoSuchAlgorithmException, IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(file), 8192);
        return md5(input);
    }

    private static String md5(InputStream input) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int i;
        while ((i = input.read(buffer)) > 0) {
            digest.update(buffer, 0, i);
        }

        return toHex(digest.digest());
    }

    private static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        String result = bi.toString(16);

        if (result.length() != 32) {
            int count = 32 - result.length();
            for (int i = 0; i < count; i++) {
                result = "0" + result;
            }
            return result;
        }
        return result;
    }
}
