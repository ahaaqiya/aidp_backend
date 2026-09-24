package com.zzccaidp.constants;

/**
 * @author zhangtiantian
 * @date 2025/3/20
 */
public class BusinessConstant {

    /*
     * 删除-mapKey
     */
    public final static String DELETE_KEY = "delete";

    /*
     * 新增-mapKey
     */
    public final static String INSERT_KEY = "insert";

    /*
     * 更新-mapKey
     */
    public final static String UPDATE_KEY = "update";

    /*
     * 成功响应信息
     */
    public final static String SUCCESS_MSG = "success";

    /*
     * 成功响应码
     */
    public final static String SUCCESS_CODE = "000000";

    /*
     * 服务错误号
     */
    public final static String SEV_ERR_CODE = "21";

    /*
     * flowId
     */
    public final static String FLOW_ID = "flowId";

    /*
     * serviceName
     */
    public final static String SERVICE_NAME = "serviceName";

    /*
     * methodName
     */
    public final static String METHOD_NAME = "methodName";

    /*
     * tradeId
     */
    public final static String TRADE_ID = "tradeId";

    /**
     * 0
     */
    public static final Integer ZERO = 0;
    /**
     * 1
     */
    public static final Integer ONE = 1;
    /**
     * 2
     */
    public static final Integer TWO = 2;

    /**
     * 阅读监控节点id前缀（未阅）
     */
    public static final String NODE_TYPE_NO_PREFIX = "no_";
    /**
     * 阅读监控节点id前缀（未阅特殊处理）
     */
    public static final String NODE_TYPE_NOREAD_PREFIX = "un_";
    /**
     * 阅读监控节点id前缀（已阅）
     */
    public static final String NODE_TYPE_AO_PREFIX = "ao_";
    /**
     * 阅读监控节点id前缀（已阅删除）
     */
    public static final String NODE_TYPE_DO_PREFIX = "do_";
    /**
     * 阅读监控节点类型（根节点）
     */
    public static final String NODE_STATUS_ROOT = "0";
    /**
     * 阅读监控节点类型（未阅）
     */
    public static final String NODE_STATUS_UNREAD = "1";
    /**
     * 阅读监控节点类型（已阅）
     */
    public static final String NODE_STATUS_READ = "2";
    /**
     * 阅读监控节点类型（已阅删除）
     */
    public static final String NODE_STATUS_DELETED = "3";
    /**
     * 未阅读的消息状态
     */
    public static final String UNREAD = "未阅";
    /**
     * 已阅读的消息状态
     */
    public static final String READ = "已阅";
    /**
     * 已阅读且被删除的消息状态
     */
    public static final String READ_DELETED = "已阅删除";
    /**
     *
     */
    public static final String SPACE = " ";

}
