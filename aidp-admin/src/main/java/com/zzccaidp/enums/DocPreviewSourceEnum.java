package com.zzccaidp.enums;

/**
 * @author zhangtiantian
 * @date 2026/5/9
 */
public enum DocPreviewSourceEnum {
    KNOW("know", "知识库"),
    SYSTEM("system", "系统文件"),
    /**
     * 知识库文档（按 document.id 主键定位，2026-11-23 新增）。
     * 与 KNOW 的区别：KNOW 按 vector_id（RAGFlow document_id）定位，仅适用已向量化文档；
     * KB_ROW 按表主键定位，未向量化文档也可预览（模板检索链路不依赖向量化）。
     */
    KB_ROW("kb_row", "知识库文档主键");

    private String val;
    private String desc;

    DocPreviewSourceEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public String getValue() {
        return val;
    }

    public String getDesc() {
        return desc;
    }

    public static DocPreviewSourceEnum getEnumByVal(String val) {
        for (DocPreviewSourceEnum docPreviewSourceEnum : DocPreviewSourceEnum.values()) {
            if (docPreviewSourceEnum.getValue().equalsIgnoreCase(val)) {
                return docPreviewSourceEnum;
            }
        }
        throw new IllegalArgumentException("No enum constant " + DocPreviewSourceEnum.class.getName() + "." + val);
    }


}
