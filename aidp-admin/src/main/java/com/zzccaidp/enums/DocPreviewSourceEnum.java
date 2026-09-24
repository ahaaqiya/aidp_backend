package com.zzccaidp.enums;

/**
 * @author zhangtiantian
 * @date 2026/5/9
 */
public enum DocPreviewSourceEnum {
    KNOW("know", "知识库"),
    SYSTEM("system", "系统文件");

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
