package com.zzccaidp.vo.ai;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description: 创建回话请求实体类，前端请求后台以及后台请求dify公用一个
 * @Author: WB233500
 * @Createtime: 10:59
 * @Version: 1.0
 */
@Getter
@Setter
public class ChatMessagesIn {
    /**
     * 允许传入 App 定义的各变量值。 inputs 参数包含了多组键值对（Key/Value pairs），每组的键对应一个特定变量，每组的值则是该变量的具体值。 默认 {}
     */

    private JSONObject inputs;
    /**
     * 用户问题
     */
    private String query;
    /**
     * streaming 流式模式（推荐）。基于 SSE（Server-Sent Events）实现类似打字机输出方式的流式返回。
     * blocking 阻塞模式，等待执行完毕后返回结果。（请求若流程较长可能会被中断）。 由于 Cloudflare 限制，请求会在 100 秒超时无返回后中断。 注：Agent模式下不允许blocking。
     */
    private String response_mode;

    /**
     * 用户标识，用于定义终端用户的身份，方便检索、统计。 由开发者定义规则，需保证用户标识在应用内唯一。服务 API 不会共享 WebApp 创建的对话。
     */
    private String user;
    /**
     * （选填）会话 ID，需要基于之前的聊天记录继续对话，必须传之前消息的 conversation_id
     */
    private String conversation_id;
    /**
     * 上传的文件。
     */
    private List<DifyFile> files;
    /**
     * （选填）自动生成标题，默认 true。 若设置为 false，则可通过调用会话重命名接口并设置 auto_generate 为 true 实现异步生成标题。
     */
    private Boolean auto_generate_name;
    /**
     * （选填）工作流ID，用于指定特定版本，如果不提供则使用默认的已发布版本。
     */
    private String workflow_id;
    /**
     * 选填）链路追踪ID。适用于与业务系统已有的trace组件打通，实现端到端分布式追踪等场景。如果未指定，系统会自动生成trace_id。支持以下三种方式传递，具体优先级依次为：
     * <p>
     * Header：通过 HTTP Header X-Trace-Id 传递，优先级最高。
     * Query 参数：通过 URL 查询参数 trace_id 传递。
     * Request Body：通过请求体字段 trace_id 传递（即本字段）。
     */
    private String trace_id;

    /***/
    public static final class DifyFile {
        /**
         * 支持类型：图片 image（目前仅支持图片格式） 。
         */
        private String type;
        /**
         * 传递方式:
         * remote_url: 图片地址。
         * local_file: 上传文件。
         */
        private String url;
        /**
         * 上传文件 ID。（仅当传递方式为 local_file 时）。
         */
        private String upload_file_id;
    }
}
