package com.pinyougou.shop.controller;

import com.pinyougou.common.util.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    @Value("$IMAGE_SERVER_URL")
    private String IMAGE_SERVER_URL;
    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        //文件的扩展名
        String originalFilename = file.getOriginalFilename();
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        //创建FastDFS的客户端
        try {
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fastdfs_client.conf");
            //上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(),extName);
            //拼接返回的url和ip地址
            String url = "http://192.168.25.133/"+path;
            return  new Result(true,url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }
    }
}
