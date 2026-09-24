package com.zzccaidp.localstub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 【本地联调桩】启动期移除 Dubbo 相关 bean 定义，使 Dubbo 完全不参与应用启动。
 * <p>
 * <b>为什么需要这个类：</b>本地没有真实注册中心、也没有可用的 Dubbo 全局配置，
 * 而 {@code appCtx-dubbo.xml} 会触发 Dubbo 两条独立的启动校验路径，都会抛
 * {@code IllegalStateException: No application config found or it's not a valid config!}：
 * <ol>
 *   <li><b>路径 A（消费引用实例化）：</b>{@code <dubbo:reference id="pullDocumentToRagFlow"/>}
 *       对应 Dubbo 的 {@code ReferenceBean}（FactoryBean），在非懒加载单例阶段被实例化时，
 *       {@code ReferenceConfig#init()} 会触发 Dubbo 全局配置校验；</li>
 *   <li><b>路径 B（上下文刷新事件）：</b>Dubbo 的 {@code DubboApplicationListenerRegistrar}
 *       （bean 名 {@code dubboApplicationListenerRegister}）会向容器注册
 *       {@code DubboBootstrapApplicationListener}，该监听器在 {@code ContextRefreshedEvent} 时
 *       调用 {@code DubboBootstrap.start()} → {@code checkGlobalConfigs()}，同样触发全局配置校验。</li>
 * </ol>
 * <p>
 * <b>本类的作用：</b>在 Bean 实例化之前的 {@link BeanFactoryPostProcessor} 阶段，
 * 把上述两类 bean 定义从容器中移除（{@code appCtx-dubbo.xml} 文件本身不做任何修改），
 * 使 Dubbo 既不创建引用代理、也不启动 Bootstrap；
 * {@code PersonalDocumentService} 对 {@code PullDocumentToRagFlow} 的依赖
 * 改由 {@link LocalPullDocumentToRagFlowStub} 注入。
 * <p>
 * <b>启用方式：</b>与同步桩共用同一个开关 {@code --aidp.local-stub.personalDocument=true}，默认关闭；
 * 不传该参数时本类不会被注册成 bean，Dubbo 引用与启动行为完全保持原样。
 * <p>
 * <b>⚠️ 本类仅供本地前端联调，严禁上线，联调结束后请直接删除本文件与 {@code com.zzccaidp.localstub} 包。</b>
 *
 * @author trae
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "aidp.local-stub.personalDocument", havingValue = "true")
public class LocalStubDubboReferenceRemover implements BeanFactoryPostProcessor {

    /** Dubbo 引用 bean 名称，对应 appCtx-dubbo.xml 中 <dubbo:reference id="pullDocumentToRagFlow"/> 的 id（拦截路径 A） */
    private static final String DUBBO_REFERENCE_BEAN_NAME = "pullDocumentToRagFlow";

    /**
     * Dubbo 应用监听器注册器 bean 名称，由 DubboBeanUtils.registerCommonBeans 自动注册，
     * 它会向容器注入 {@code DubboBootstrapApplicationListener}（拦截路径 B）。
     */
    private static final String DUBBO_APPLICATION_LISTENER_REGISTRAR_BEAN_NAME = "dubboApplicationListenerRegister";

    /**
     * Dubbo Bootstrap 监听器类名；作为兜底，若容器中存在该类型的 bean 定义也一并移除，
     * 防止不同 Dubbo 小版本下注册方式变化导致监听器仍然生效。
     */
    private static final String DUBBO_BOOTSTRAP_LISTENER_CLASS_NAME =
            "org.apache.dubbo.config.spring.context.DubboBootstrapApplicationListener";

    /**
     * 在所有单例 Bean 实例化之前执行，移除 Dubbo 引用 bean 与应用监听器注册器，
     * 避免启动期触发 Dubbo 全局配置校验（详见类注释中的路径 A / 路径 B）。
     *
     * @param beanFactory Spring bean 工厂（实际类型为 DefaultListableBeanFactory，同时实现了 BeanDefinitionRegistry）
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry)) {
            log.warn("[本地联调桩] 当前 BeanFactory 不是 BeanDefinitionRegistry，无法移除 Dubbo 相关 bean，"
                    + "请检查 Spring 容器类型。");
            return;
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        // 路径 A：移除消费引用 bean，避免 ReferenceBean 实例化触发校验
        removeIfPresent(registry, DUBBO_REFERENCE_BEAN_NAME,
                "启动期不再创建 ReferenceBean，该接口由 LocalPullDocumentToRagFlowStub 兜底注入");

        // 路径 B：移除监听器注册器，避免 DubboBootstrapApplicationListener 在上下文刷新时启动 Dubbo
        removeIfPresent(registry, DUBBO_APPLICATION_LISTENER_REGISTRAR_BEAN_NAME,
                "DubboBootstrapApplicationListener 不再注册，DubboBootstrap 不会在上下文刷新时启动");

        // 兜底：不同 Dubbo 小版本可能直接以 bean 形式注册监听器，按类型名再清一次
        removeByClassName(registry, DUBBO_BOOTSTRAP_LISTENER_CLASS_NAME,
                "兜底移除已直接注册的 DubboBootstrapApplicationListener");
    }

    /**
     * 按 bean 名称移除 bean 定义；不存在时静默跳过（Dubbo 未参与即符合本桩预期）。
     *
     * @param registry Spring bean 定义注册表
     * @param beanName 待移除的 bean 名称
     * @param effect   移除成功后打印的生效说明
     */
    private void removeIfPresent(BeanDefinitionRegistry registry, String beanName, String effect) {
        if (!registry.containsBeanDefinition(beanName)) {
            return;
        }
        registry.removeBeanDefinition(beanName);
        log.warn("[本地联调桩] 已移除 Dubbo bean『{}』：{}。本桩严禁用于生产环境！", beanName, effect);
    }

    /**
     * 按 bean 定义的类名移除 bean 定义，用于兜底清理自动注册的 Dubbo 监听器。
     *
     * @param registry  Spring bean 定义注册表
     * @param className 待移除 bean 的完整类名
     * @param effect    移除成功后打印的生效说明
     */
    private void removeByClassName(BeanDefinitionRegistry registry, String className, String effect) {
        for (String beanName : registry.getBeanDefinitionNames()) {
            if (!registry.containsBeanDefinition(beanName)) {
                continue;
            }
            String beanClassName = registry.getBeanDefinition(beanName).getBeanClassName();
            if (className.equals(beanClassName)) {
                registry.removeBeanDefinition(beanName);
                log.warn("[本地联调桩] 已按类名移除 Dubbo bean『{}』（{}）：{}。本桩严禁用于生产环境！",
                        beanName, className, effect);
            }
        }
    }
}
