// 文件路径: backend/src/main/java/com/lei/springbootpractice4/controller/FileController.java
package com.lei.springbootpractice4.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource; // <--- 引入 Resource
import org.springframework.core.io.UrlResource; // <--- 引入 UrlResource
import org.springframework.http.HttpHeaders; // <--- 引入 HttpHeaders
import org.springframework.http.MediaType; // <--- 引入 MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // <--- 引入 GetMapping, PathVariable
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException; // <--- 引入 MalformedURLException
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.net.URLEncoder; // <--- 引入 URLEncoder
import java.nio.charset.StandardCharsets; // <--- 引入 StandardCharsets

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDirConfig;

    // --- uploadFile 方法保持不变 ---
    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
        // ... (代码保持不变) ...
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("上传的文件不能为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            File configuredUploadDir = new File(uploadDirConfig);
            Path absoluteUploadPath = Paths.get(configuredUploadDir.getAbsolutePath());
            Path targetFilePath = absoluteUploadPath.resolve(uniqueFileName);

            if (!Files.exists(absoluteUploadPath)) {
                Files.createDirectories(absoluteUploadPath);
                System.out.println("创建上传目录: " + absoluteUploadPath);
            }

            File destFile = targetFilePath.toFile();
            file.transferTo(destFile);
            System.out.println("文件已保存到: " + destFile.getAbsolutePath());

            Map<String, Object> response = new HashMap<>();
            response.put("uniqueFileName", uniqueFileName);
            response.put("originalFileName", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("fileType", file.getContentType());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件上传失败: " + e.getMessage());
        }
    }

    // --- 新增：文件下载接口 ---
    @GetMapping("/download/{uniqueFileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String uniqueFileName,
                                                 @RequestParam(required = false) String originalName) { // 可选参数 originalName 用于显示
        try {
            // 1. 构建文件的绝对路径
            File configuredUploadDir = new File(uploadDirConfig);
            Path absoluteUploadPath = Paths.get(configuredUploadDir.getAbsolutePath());
            Path filePath = absoluteUploadPath.resolve(uniqueFileName);
            Resource resource = new UrlResource(filePath.toUri());

            // 2. 检查文件是否存在且可读
            if (resource.exists() && resource.isReadable()) {
                // 3. 确定原始文件名 (如果前端未提供，则使用 uniqueFileName)
                String downloadFilename = (originalName != null && !originalName.isBlank()) ? originalName : uniqueFileName;
                // 对中文文件名进行 URL 编码，防止乱码
                String encodedFilename = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");


                // 4. 设置响应头
                HttpHeaders headers = new HttpHeaders();
                // 关键：Content-Disposition = attachment 表示下载，filename*=UTF-8''... 处理中文名
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
                // 可以尝试根据文件后缀名确定 MediaType，或使用通用类型
                // headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

                // 5. 返回文件资源
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                System.err.println("请求的文件不存在或无法读取: " + filePath);
                return ResponseEntity.status(404).body(null); // 文件未找到
            }
        } catch (MalformedURLException e) {
            System.err.println("构建文件URL失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null); // 服务器内部错误
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null); // 服务器内部错误
        }
    }
    // --- 新增结束 ---
}