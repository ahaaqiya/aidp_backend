package com.zzccaidp.enums;

/**
 * 文件类型枚举
 *
 * @author
 */
public enum FileType {
    /**
     * JPEG.
     */
    JPEG("FFD8FF", "JPEG图片"),

    /**
     * PNG.
     */
    PNG("89504E47", "PNG图片"),

    /**
     * GIF.
     */
    GIF("47494638", "GIF图片"),

    /**
     * TIFF.
     */
    TIFF("49492A00", "TIFF图片"),

    /**
     * Windows Bitmap.
     */
    BMP("424D", "BMP图片"),

    /**
     * CAD.
     */
    DWG("41433130", "CAD文件"),

    /**
     * Adobe Photoshop.
     */
    PSD("38425053", "photoshop 文件"),

    /**
     * Rich Text Format.
     */
    RTF("7B5C727466", "富文本"),

    /**
     * XML.
     */
    XML("3C3F786D6C", "XML"),

    /**
     * HTML.
     */
    HTML("68746D6C3E", "html"),

    /**
     * Email [thorough only].
     */
    EML("44656C69766572792D646174653A", "email"),

    /**
     * Outlook Express.
     */
    DBX("CFAD12FEC5FD746F", "outlook"),

    /**
     * Outlook (pst).
     */
    PST("2142444E", "pst"),

    /**
     * MS Word/Excel.
     */
    XLS_DOC("D0CF11E0", "MS WORD/EXCEL"),

    /**
     * MS Access.
     */
    MDB("5374616E64617264204A", "MS Access"),

    /**
     * WordPerfect.
     */
    WPD("FF575043", "WordPerfect"),

    /**
     * Postscript.
     */
    EPS("252150532D41646F6265", "Postscript"),

    /**
     * Adobe Acrobat.
     */
    PDF("255044462D312E", "Adobe Acrobat"),

    /**
     * Quicken.
     */
    QDF("AC9EBD8F", "Quicken"),

    /**
     * Windows Password.
     */
    PWL("E3828596", "Windows Password"),

    /**
     * ZIP Archive.
     */
    ZIP("504B0304", "ZIP Archive"),

    /**
     * RAR Archive.
     */
    RAR("52617221", "RAR Archive"),

    /**
     * Wave.
     */
    WAV("57415645", "Wave"),

    /**
     * AVI.
     */
    AVI("41564920", "AVI"),

    /**
     * Real Audio.
     */
    RAM("2E7261FD", "Real Audio"),

    /**
     * Real Media.
     */
    RM("2E524D46", "Real Media"),

    /**
     * MPEG (mpg).
     */
    MPG("000001BA", "MPEG"),

    /**
     * Quicktime.
     */
    MOV("6D6F6F76", "Quicktime"),

    /**
     * Windows Media.
     */
    ASF("3026B2758E66CF11", "Windows Media"),

    /**
     * MIDI.
     */
    MID("4D546864", "MIDI");

    private String val;
    private String desc;

    private FileType(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public String getValue() {
        return val;
    }

    public String getDesc() {
        return desc;
    }

}
