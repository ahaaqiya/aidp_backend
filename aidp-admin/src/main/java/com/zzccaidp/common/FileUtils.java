package com.zzccaidp.common;

import com.zzccaidp.enums.FileType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件操作工具类
 *
 */
@Slf4j
public class FileUtils {

    /**
     * 将文件头转换成16进制字符串
     *
     * @param src 元数据
     * @return 格式化为16进制的字符串
     */
    private static String bytesToHexString(byte[] src) {

        StringBuilder stringBuilder = new StringBuilder();
        if ((src == null) || (src.length <= 0)) {
            return null;
        }
        final int len = src.length;
        for (int i = 0; i < len; i++) {
            final int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 获得文件的Content
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String getFileContent(InputStream inputStream) throws IOException {

        byte[] buffer = null;
        final int lenth = 28;
        buffer = new byte[lenth];
        int num_read = 0;
        while (num_read < lenth) {
            final int count = inputStream.read(buffer, num_read, lenth - num_read);
            if (count < 0) {
                throw new IOException("end of stream reached");
            }
            num_read += count;
        }

        return bytesToHexString(buffer);
    }

    /**
     * 获得文件的Content
     *
     * @param filePath 文件路径
     * @return
     * @throws IOException
     */
    public static String getFileContent(String filePath) throws IOException {

        String result = "";
        try (InputStream inputStream = new FileInputStream(filePath)) {
            result = getFileContent(inputStream);
        }
        return result;
    }

    /**
     * 获得文件类型
     *
     * @param inputStream
     * @return 文件类型 {@linkFileType}
     * @throws IOException
     */
    public static FileType getType(InputStream inputStream) throws IOException {

        String fileHead = "";
        try {
            fileHead = getFileContent(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        if (StringUtils.isBlank(fileHead)) {
            return null;
        }

        if (fileHead != null) {
            fileHead = fileHead.toUpperCase();
        } else {
            fileHead = "";
        }

        FileType[] fileTypes = FileType.values();

        for (FileType type : fileTypes) {
            if (fileHead.startsWith(type.getValue())) {
                return type;
            }
        }

        return null;
    }

    /**
     * 获得文件类型
     *
     * @param filePath
     * @return 文件类型 {@linkFileType}
     * @throws IOException
     */
    public static FileType getType(String filePath) throws IOException {
        FileType result = null;
        try (InputStream inputStream = new FileInputStream(filePath)) {
            result = getType(inputStream);
        }
        return result;
    }

    /**
     * 判断文件类型是否在允许范围内
     *
     * @param filePath    文件路径
     * @param allowdTypes 允许的列表
     * @return
     * @throws IOException
     * @author 明帅
     */
    public static boolean isAllowed(String filePath, Set<FileType> allowdTypes) throws IOException {
        boolean result = false;
        try (InputStream inputStream = new FileInputStream(filePath)) {
            result = isAllowed(inputStream, allowdTypes);
        }
        return result;
    }

    /**
     * yangyang
     *
     * @param inputStream 文件输入流
     * @param allowdTypes 允许的列表
     * @return
     * @throws IOException
     */
    public static boolean isAllowed(InputStream inputStream, Set<FileType> allowdTypes) throws IOException {
        FileType type = getType(inputStream);
        if (allowdTypes.contains(type)) {
            return true;
        }
        return false;
    }

    /**
     * 判断文件后缀是否匹配
     * WB1030
     */
    public static boolean isMatchd(String fileName, Set<FileType> allowdTypes) throws IOException {
        if (fileName.indexOf(".") != -1) {
            String type = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
            FileType t;
            try {
                if ("DOC".equalsIgnoreCase(type) || "DOCX".equalsIgnoreCase(type) || "XLS".equalsIgnoreCase(type) || "XLSX".equalsIgnoreCase(type)) {
                    return true;
                } else {
                    t = Enum.valueOf(FileType.class, type);
                    if (allowdTypes.contains(t)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 文件下载到客户端
     *
     * @param fileName 文件名
     * @param bytes    元数据
     * @param resp     response
     * @throws IOException
     */
    public static void defaultDownLoadFile(String fileName, byte[] bytes, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        resp.addHeader("Content-Length", String.valueOf(bytes.length));
        resp.setContentType(new MimetypesFileTypeMap().getContentType(fileName));
        try (ServletOutputStream output = resp.getOutputStream()) {
            IOUtils.write(bytes, output);
            output.flush();
        } catch (IOException e) {
            log.error(null, e);
        }
    }

    public static void perviewFile(String fileName, byte[] bytes, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Disposition", "inline;filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        try (ServletOutputStream output = resp.getOutputStream()) {
            IOUtils.write(bytes, output);
            output.flush();
        } catch (IOException e) {
            log.error(null, e);
        }
    }

    /**
     * 文件下载到服务器
     *
     * @param fileName 文件名
     * @param bytes    元数据
     * @throws IOException
     */
    public static void defaultDownLoadFile(String fileName, byte[] bytes) throws IOException {
        File file = new File(fileName.substring(0, fileName.lastIndexOf("/") + 1));
        if (!file.exists()) {
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(true, false);
            file.mkdirs();
        }

        try (FileOutputStream output = new FileOutputStream(fileName)) {
            IOUtils.write(bytes, output);
            File saveFile = new File(fileName);
            log.info("文件写入后赋权");
            saveFile.setReadable(true, false);
            saveFile.setWritable(true, false);
            saveFile.setExecutable(true, false);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定目录下文件及文件夹
     *
     * @param filePath
     */
    public static void delete(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                //解决关闭输出输入流后，依然无法删除文件
                System.gc();
                Files.walkFileTree(Paths.get(filePath),
                        new SimpleFileVisitor<Path>() {
                            //  先遍历删除文件
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                                try {
                                    Files.delete(file);
                                    log.info("删除文件：{}", file);
                                } catch (Exception e) {
                                    log.error("删除文件失败" + file, e);
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            // 再遍历删除目录
                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                                try {
                                    Files.delete(dir);
                                    log.info("删除文件夹：{}", dir);
                                } catch (Exception e) {
                                    log.error("删除文件夹失败" + file, e);
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        }
                );
            }
        } catch (Exception e) {
            log.error("FileDelete_ERROR", e);
        }

    }

    /**
     * 压缩文件
     *
     * @param sourceFilePath
     * @param zipFilePath
     * @return
     * @throws IOException
     */
    public static File fileToZip(String sourceFilePath, String zipFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File zipFile = new File(zipFilePath);

        log.info("fileToZip开始压缩文件，sourceFilePath：{}，zipFilePath：{}", sourceFilePath, zipFilePath);
        if (!sourceFile.exists()) {
            log.info("待压缩的文件目录{}，不存在", sourceFilePath);
        } else {
            FileOutputStream fos = null;
            ZipOutputStream zos = null;

            try {
                if (zipFile.exists()) {
                    Files.delete(zipFile.toPath());
                }
                File[] sourceFiles = sourceFile.listFiles();
                if (null == sourceFiles || sourceFiles.length < 1) {
                    log.info("待压缩的文件目录：{}里面不存在文件，无须压缩", sourceFilePath);
                } else {
                    fos = new FileOutputStream(zipFile);
                    zos = new ZipOutputStream(new BufferedOutputStream(fos));

                    for (File file : sourceFiles) {
                        //创建zip实体，并添加进压缩包
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);
                        //读取待压缩文件并写进压缩包
                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buff = new byte[2048];
                            int bytesRead;
                            while (-1 != (bytesRead = fis.read(buff, 0, buff.length))) {
                                zos.write(buff, 0, bytesRead);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("fileToZip发生异常", e);
            } finally {
                if (zos != null) {
                    zos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        }
        return zipFile;
    }

    /**
     * 获取文件后缀
     *
     * @param fileName
     * @return
     */
    public static String getSuffix(String fileName) {
        String fileType = null;
        if (StringUtils.isNotEmpty(fileName) && fileName.contains(".")) {
            fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length()).toLowerCase();
        }

        return fileType;
    }

    /**
     * 将字符串文本转换为MultipartFile文件
     * @param content
     * @param filename
     * @param contentType
     * @return
     * @throws IOException
     */
    public static MultipartFile convertStringToMultipartFile(String content, String filename, String contentType) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        return new MockMultipartFile(filename, filename, contentType, bis);
    }
}
