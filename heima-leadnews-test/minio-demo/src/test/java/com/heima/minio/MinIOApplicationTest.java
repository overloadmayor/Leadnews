package com.heima.minio;

import com.heima.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MinIOApplication.class)
@RunWith(SpringRunner.class)
public class MinIOApplicationTest {
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream("D:/桌面/list.html");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String path = fileStorageService.uploadHtmlFile("", "list1.html", fileInputStream);
        System.out.println(path);
    }
//}

//class MinIOApplicationTest {
//    /**
//     * 把list.html文件上传到minio中，并在浏览器中访问
//     * @param args
//     */
    @Test
    public void maintest() {

        try {
            FileInputStream fileInputStream=new FileInputStream("D:/HeiMaNews/js/index.js");

            //获取minio的链接信息，创建一个minio的客户端
            MinioClient minioClient =
                    MinioClient.builder().credentials("minio", "minio123").endpoint("http" +
                            "://192.168.200" +
                    ".130:9000").build();

            //上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("plugins/js/index.js") //文件名词
                    .contentType("text/javascript") //文件类型
                    .bucket("leadnews") //桶名称
                    .stream(fileInputStream, fileInputStream.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);

            //访问路径
            System.out.println("http://192.168.200.130:9000/leadnews/list.html");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}