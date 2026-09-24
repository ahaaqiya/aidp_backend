package com.zzccaidp.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangtiantian
 * @date 2026/2/9
 */
public class FileTypeDetector {
    // 文件类型映射
    public static final Map<String, String> FILE_TYPE_MAP = new HashMap<>();

    static {

        // 文字文件
        FILE_TYPE_MAP.put("txt", "w");
        FILE_TYPE_MAP.put("doc", "w");
        FILE_TYPE_MAP.put("docx", "w");
        FILE_TYPE_MAP.put("rtf", "w");
        FILE_TYPE_MAP.put("odt", "w");
        FILE_TYPE_MAP.put("dot", "w");
        FILE_TYPE_MAP.put("wps", "w");
        FILE_TYPE_MAP.put("wpt", "w");
        FILE_TYPE_MAP.put("dotx", "w");
        FILE_TYPE_MAP.put("docm", "w");
        FILE_TYPE_MAP.put("dotm", "w");

        // 表格文件
        FILE_TYPE_MAP.put("xls", "s");
        FILE_TYPE_MAP.put("xlt", "s");
        FILE_TYPE_MAP.put("et", "s");
        FILE_TYPE_MAP.put("xlsx", "s");
        FILE_TYPE_MAP.put("xltx", "s");
        FILE_TYPE_MAP.put("csv", "s");
        FILE_TYPE_MAP.put("xlsm", "s");
        FILE_TYPE_MAP.put("xltm", "s");

        // 演示文件
        FILE_TYPE_MAP.put("ppt", "p");
        FILE_TYPE_MAP.put("pptm", "p");
        FILE_TYPE_MAP.put("pptx", "p");
        FILE_TYPE_MAP.put("ppsx", "p");
        FILE_TYPE_MAP.put("ppsm", "p");
        FILE_TYPE_MAP.put("pps", "p");
        FILE_TYPE_MAP.put("potx", "p");
        FILE_TYPE_MAP.put("potm", "p");
        FILE_TYPE_MAP.put("dpt", "p");
        FILE_TYPE_MAP.put("dps", "p");

        // PDF文件
        FILE_TYPE_MAP.put("pdf", "f");
        FILE_TYPE_MAP.put("ofd", "f");

        // 图片、压缩包、其他
        FILE_TYPE_MAP.put("jpg", "x");
        FILE_TYPE_MAP.put("jpeg", "x");
        FILE_TYPE_MAP.put("png", "x");
        FILE_TYPE_MAP.put("bmp", "x");
        FILE_TYPE_MAP.put("zip", "x");
        FILE_TYPE_MAP.put("rar", "x");
        FILE_TYPE_MAP.put("tar", "x");
        FILE_TYPE_MAP.put("gz", "x");
        FILE_TYPE_MAP.put("7z", "x");
    }
}
