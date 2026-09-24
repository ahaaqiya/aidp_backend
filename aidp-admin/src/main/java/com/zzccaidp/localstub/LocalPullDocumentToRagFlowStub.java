package com.zzccaidp.localstub;

import com.athena.api.PullDocumentToRagFlow;
import com.athena.api.out.PersonalDocSyncRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 【本地联调桩】zsk 文档推送 Dubbo 接口 {@link PullDocumentToRagFlow} 的空实现。
 * <p>
 * <b>为什么需要这个类：</b>本地无真实 Dubbo 注册中心（{@code dubbo.registry.address} 默认 {@code N/A}，
 * 真实地址在 Apollo）、也未启动 zsk 侧 provider；而 {@code appCtx-dubbo.xml} 中的
 * {@code <dubbo:reference id="pullDocumentToRagFlow"/>} 会在应用启动阶段创建 FactoryBean 并触发
 * {@code ReferenceConfig#init()}，Dubbo 2.7.23 在 {@code DubboBootstrap#checkGlobalConfigs()} 中
 * 强校验「必须存在 ApplicationConfig」，本地直接抛
 * {@code IllegalStateException: No application config found or it's not a valid config!}，
 * 导致 <b>整个应用启动失败</b>（与是否使用同步桩无关，属存量配置问题）。
 * <p>
 * <b>本类的作用：</b>提供 {@code PullDocumentToRagFlow} 类型的本地空实现，
 * 配合 {@link LocalStubDubboReferenceRemover} 移除 XML 中的 Dubbo 引用 bean，
 * 使 {@code PersonalDocumentService} 中
 * {@code @Autowired private PullDocumentToRagFlow pullDocumentToRagFlow;} 改由本桩注入，
 * 从而应用可以正常启动、Dubbo 不参与启动流程。
 * <p>
 * <b>启用方式：</b>与同步桩共用同一个开关 {@code --aidp.local-stub.personalDocument=true}，默认关闭。
 * <p>
 * <b>⚠️ 本类仅供本地前端联调，严禁上线，联调结束后请直接删除本文件与 {@code com.zzccaidp.localstub} 包。</b>
 *
 * @author trae
 */
@Slf4j
@Service("localPullDocumentToRagFlowStub")
@Primary
@ConditionalOnProperty(name = "aidp.local-stub.personalDocument", havingValue = "true")
public class LocalPullDocumentToRagFlowStub implements PullDocumentToRagFlow {

    /**
     * 空实现：只打印日志，不做任何远程调用。
     * <p>
     * 注意：同步桩 {@link LocalPersonalDocumentStub#sync()} 已整体覆盖同步流程，
     * 正常情况下本方法不会被触发；保留此方法仅为满足 {@code PersonalDocumentService} 的依赖注入，
     * 若被触发说明有代码绕过了同步桩直接走真实链路。
     *
     * @param userName  用户工号
     * @param datasetId AIDP 侧分配的 RAGFlow 数据集ID
     * @return 固定的空成功结果（0 篇），避免真实链路被触发时写出假成功计数
     */
    @Override
    public PersonalDocSyncRes pullPersonalDocument(String userName, String datasetId) {
        log.warn("[本地联调桩] 拦截 zsk Dubbo 推送接口调用（userName={}，datasetId={}）：未发起任何远程调用。",
                userName, datasetId);
        return PersonalDocSyncRes.ok(0, 0, "本地联调桩：未发起远程调用");
    }

    /**
     * 空实现：拦截「按指定起点重放（重新同步）」调用，未发起任何远程调用（本地联调桩行为，与上方方法一致）。
     *
     * @param userName  用户工号
     * @param datasetId AIDP 侧分配的 RAGFlow 数据集ID
     * @param startTime 重放起点时间（yyyy-MM-dd HH:mm:ss）
     * @return 固定的空成功结果（0 篇），避免真实链路被触发时写出假成功计数
     */
    @Override
    public PersonalDocSyncRes pullPersonalDocumentFrom(String userName, String datasetId, String startTime) {
        log.warn("[本地联调桩] 拦截 zsk Dubbo 重放接口调用（userName={}，datasetId={}，startTime={}）：未发起任何远程调用。",
                userName, datasetId, startTime);
        return PersonalDocSyncRes.ok(0, 0, "本地联调桩：未发起远程调用");
    }

    /**
     * 空实现：拦截「查询上次同步水位时间」调用，未发起任何远程调用。
     *
     * @param userName 用户工号
     * @return 固定 null（本地桩无水位数据，前端按「尚未同步过」展示）
     */
    @Override
    public String getLastPersonalSyncTime(String userName) {
        log.warn("[本地联调桩] 拦截 zsk Dubbo 水位查询接口调用（userName={}）：未发起任何远程调用。", userName);
        return null;
    }
}
