package com.kmbeast.controller;

import com.kmbeast.utils.PathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件前端控制器
 * 引入 NIO 零拷贝传输 与 HTTP 强缓存，支持大文件高并发分发
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private static final String UPLOAD_DIR = "/file"; // 保存路径
    private static final String FILE_PARAM = "file"; // 上传文件名
    private static final String FILE_NAME_PARAM = "fileName"; // 参数名

    @Value("${my-server.api-context-path}")
    private String apiContextPath;

    /**
     * 文件上传逻辑（保持不变）
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam(FILE_PARAM) MultipartFile multipartFile) {
        return handleFileUpload(multipartFile);
    }

    /**
     * 查看/下载文件资源
     * 优化点：1. NIO Zero-Copy (减少CPU损耗) 2. Cache-Control (减少带宽压力)
     */
    @GetMapping("/getFile")
    public void getFile(@RequestParam(FILE_NAME_PARAM) String fileName,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        // 1. 跨域处理与预检请求响应
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        File file = getUploadedFile(fileName);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // --- 【性能优化 A：针对高并发的缓存策略】 ---
        // 设置强缓存，单位为秒。这里设为 30 天 (2592000秒)
        // 意义：高并发下，同一个用户的多次访问将直接读取其本地磁盘缓存，不再请求服务器，节省 100% 的带宽
        response.setHeader("Cache-Control", "public, max-age=2592000");

        // 2. 根据后缀名动态识别 Content-Type
        String lowerName = fileName.toLowerCase();
        String contentType;
        if (lowerName.endsWith(".glb")) {
            contentType = "model/gltf-binary";
        } else if (lowerName.endsWith(".gltf")) {
            contentType = "model/gltf+json";
        } else if (lowerName.endsWith(".png")) {
            contentType = "image/png";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lowerName.endsWith(".gif")) {
            contentType = "image/gif";
        } else {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        // --- 【性能优化 B：引入 NIO 零拷贝技术 (Zero-Copy)】 ---
        // 传统 IO：磁盘 -> 内核缓冲区 -> Java堆内存 -> Socket缓冲区 -> 网卡
        // NIO transferTo：磁盘 -> 内核缓冲区 -> 网卡 (直连，不经过Java内存)
        // 意义：极大降低大文件（30MB+）传输时的 CPU 负载和内存拷贝开销，防止高并发时服务器卡死
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel();
             OutputStream os = response.getOutputStream()) {

            // 将 ServletOutputStream 包装成 NIO 写入通道
            WritableByteChannel writableByteChannel = Channels.newChannel(os);

            // 执行零拷贝传输：从 0 位置开始，传输整个文件长度
            fileChannel.transferTo(0, fileChannel.size(), writableByteChannel);
        }
//        byte[] buffer = new byte[8192];
//        int bytesRead;
//        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
    }

    /**
     * 处理文件上传逻辑
     */
    private Map<String, Object> handleFileUpload(MultipartFile multipartFile) {
        Map<String, Object> response = new HashMap<>();
        if (multipartFile.isEmpty()) {
            response.put("code", 400);
            response.put("msg", "文件不能为空");
            return response;
        }

        long currentTimeMillis = System.currentTimeMillis();
        String fileName = currentTimeMillis + multipartFile.getOriginalFilename();
        try {
            if (saveUploadedFile(multipartFile, fileName)) {
                response.put("code", 200);
                response.put("data", apiContextPath + "/file/getFile?" + FILE_NAME_PARAM + "=" + fileName);
                return response;
            }
        } catch (IOException e) {
            response.put("code", 500);
            response.put("msg", "文件上传失败");
            return response;
        }

        response.put("code", 500);
        response.put("msg", "文件上传失败");
        return response;
    }

    /**
     * 保存上传的文件
     */
    private boolean saveUploadedFile(MultipartFile multipartFile, String fileName) throws IOException {
        File uploadDir = new File(PathUtils.getClassLoadRootPath() + UPLOAD_DIR);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            return false;
        }

        File destFile = new File(uploadDir, fileName);
        if (destFile.exists() && !destFile.delete()) {
            return false;
        }

        multipartFile.transferTo(destFile);
        return true;
    }

    /**
     * 获取已上传的文件对象
     */
    private File getUploadedFile(String fileName) {
        File uploadDir = new File(PathUtils.getClassLoadRootPath() + UPLOAD_DIR);
        return new File(uploadDir, fileName);
    }
}