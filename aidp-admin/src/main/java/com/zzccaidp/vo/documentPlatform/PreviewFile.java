package com.zzccaidp.vo.documentPlatform;

import lombok.Data;

/**
 * @author zhangtiantian
 * @date 2026/2/9
 */
@Data
public class PreviewFile {

    private String id;

    private String name;

    private Integer version;

    private Integer size;

    private String creator;

    private Integer create_time;

    private String modifier;

    private Integer modify_time;

    private String download_url;

    private Integer preview_pages;
}
