package com.csy.springbootauthbe.common.aws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AwsService awsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        awsService.bucketName = "test-bucket";
    }

    // -------------------- uploadFile --------------------

    @Test
    void testUploadFile_Success() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("test.png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getSize()).thenReturn(100L);

        PutObjectResponse mockResponse = mock(PutObjectResponse.class);
        when(mockResponse.checksumSHA256()).thenReturn("abc123");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockResponse);

        AwsResponse result = awsService.uploadFile(multipartFile, "folder1");

        assertNotNull(result);
        assertEquals("abc123", result.getHash());
        assertTrue(result.getKey().startsWith("folder1/"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_ThrowsIOException() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("test.png");
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO fail"));

        assertThrows(RuntimeException.class, () -> awsService.uploadFile(multipartFile, "folder2"));
    }

    // -------------------- uploadProfilePic --------------------

    @Test
    void testUploadProfilePic_Success() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.jpg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getSize()).thenReturn(50L);

        PutObjectResponse mockResponse = mock(PutObjectResponse.class);
        when(mockResponse.checksumSHA256()).thenReturn("hashXYZ");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockResponse);

        AwsResponse res = awsService.uploadProfilePic(multipartFile, "profile");

        assertNotNull(res);
        assertEquals("hashXYZ", res.getHash());
        assertTrue(res.getKey().contains("profile/"));
        assertTrue(res.getKey().contains("_avatar.jpg"));
    }

    @Test
    void testUploadProfilePic_ThrowsIOException() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("broken.png");
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO fail"));

        assertThrows(RuntimeException.class, () -> awsService.uploadProfilePic(multipartFile, "profile"));
    }

    // -------------------- deleteFile --------------------

    @Test
    void testDeleteFile_Success() {
        DeleteObjectResponse mockResponse = mock(DeleteObjectResponse.class);
        when(s3Client.deleteObject(any(java.util.function.Consumer.class))).thenReturn(mockResponse);

        awsService.deleteFile("test/key.png");

        verify(s3Client).deleteObject(any(java.util.function.Consumer.class));
    }

    @Test
    void testDeleteFile_NullKey_NoAction() {
        awsService.deleteFile(null);
        verifyNoInteractions(s3Client);
    }

    @Test
    void testDeleteFile_ThrowsException() {
        doThrow(new RuntimeException("S3 fail"))
            .when(s3Client)
            .deleteObject(any(java.util.function.Consumer.class));

        assertThrows(RuntimeException.class, () -> awsService.deleteFile("some/key"));
    }

    // -------------------- deleteProfilePic --------------------

    @Test
    void testDeleteProfilePic_Success() {
        DeleteObjectResponse mockResponse = mock(DeleteObjectResponse.class);
        when(s3Client.deleteObject(any(java.util.function.Consumer.class))).thenReturn(mockResponse);

        awsService.deleteProfilePic("img/profile.jpg");

        verify(s3Client).deleteObject(any(java.util.function.Consumer.class));
    }

    @Test
    void testDeleteProfilePic_Failure() {
        doThrow(new RuntimeException("delete fail"))
            .when(s3Client)
            .deleteObject(any(java.util.function.Consumer.class));

        assertThrows(RuntimeException.class, () -> awsService.deleteProfilePic("broken/key"));
    }

    // -------------------- extractKeyFromUrl --------------------

    @Test
    void testExtractKeyFromUrl_Valid() {
        String url = "https://mybucket.s3.us-east-1.amazonaws.com/profile/abc123.jpg";
        String key = awsService.extractKeyFromUrl(url);

        assertEquals("profile/abc123.jpg", key);
    }

    @Test
    void testExtractKeyFromUrl_InvalidUrl() {
        String key = awsService.extractKeyFromUrl("https://google.com/file");
        assertNull(key);
    }

    @Test
    void testExtractKeyFromUrl_Null() {
        assertNull(awsService.extractKeyFromUrl(null));
    }
}
