package com.zzccaidp.common;

/**
 * @Description: 全局静态变量
 * @Author: WB233500
 * @Createtime: 08:56
 * @Version: 1.0
 */
public class ConfigPropertieCommon {

    public static final String SESSION_TIME = "SESSION_TIME";


    public static final String LOGIN_WHITE_URI = "LOGIN_WHITE_URI";


    public static final String AUTHORIZATION = "Authorization";
    /**
     * Dify创建回话url
     */
    public static final String CHAT_MESSAGES = "/chat-messages";


    public static final String STOP_CHAT = "/chat-messages/%s/stop";


    /**
     * Dify查询历史会话url
     */
    public static final String CONVERSATIONS = "/conversations";

    /**
     * Dify查询回话历史消息url
     */
    public static final String MESSAGES = "/messages";

    public static final String AGENTSCOEP_CHAT_STREAM = "/conversations/messages/stream";

    public static final String AGENTSCOEP_SESSION = "/conversations?user_id={userId}";

    public static final String AGENTSCOEP_SESSION_MESSAGE = "/conversations/{sessionId}/messages ";

    public static final String AGENTSCOEP_DELETE_SESSION = "/conversations/{sessionId}";

    public static final String AGENTSCOEP_STOP_SESSION = "/conversations/{generationId}/stop";

    public static final String RESULT_CODE = "resultcode";
    public static final String RESULT_MSG = "resultmsg";
    //文件路径分隔符
    public static final String FILE_PATH_SEPARATOR = "FILE_PATH_SEPARATOR";

}
