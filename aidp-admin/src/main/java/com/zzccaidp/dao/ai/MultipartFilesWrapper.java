package com.zzccaidp.dao.ai;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MultipartFilesWrapper {
    //要识别的文件
    MultipartFile wfile;
    //模板文件
    MultipartFile template;
    //提示词文件
    MultipartFile prompt;

    public MultipartFilesWrapper(MultipartFile wfile, MultipartFile template, MultipartFile prompt) {
        this.wfile = wfile;
        this.template = template;
        this.prompt = prompt;
    }
}