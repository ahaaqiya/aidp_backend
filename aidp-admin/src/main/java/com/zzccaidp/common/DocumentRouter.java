package com.zzccaidp.common;

import com.amazonaws.services.s3.model.RoutingRule;
import lombok.Getter;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @Description: 文档类型路由类
 * @Author: WB233500
 * @Createtime: 14:13
 * @Version: 1.0
 */
public class DocumentRouter {

    private static List<RoutingRule> rules;

    public static void initRules() {
        List<RoutingRule> routingRuleList = new ArrayList<>();
        routingRuleList.add(new RoutingRule(1, "compliance"
                , "(制度|规定|办法|细则|指引|合规|风险|内容|审计|监督|条例|罚则|红线|SOP(?!.*营销)|操作风险|信用风险|合规手册)"));
        routingRuleList.add(new RoutingRule(2, "research"
                , "(研报|研究报告|行业报告|市场分析|宏观分析|策略报告|调研|月报|季报|年报|观点|预测|固执|投资策略|资产配置|专题报告|晨会|周报)"));
        routingRuleList.add(new RoutingRule(3, "product"
                , "(产品说明书|产品合同|产品协议|产品公告|净值公共|产品到期|产品成立|产品发行|招募说明书|产品要素表|产品运作报告)"));
        routingRuleList.add(new RoutingRule(4, "marketing"
                , "(宣传|营销|话术|推广|海报|册子|折页|销售指引|渠道物料|公众号|推文|广告|促销|营销方案|品牌活动)"));
        routingRuleList.add(new RoutingRule(5, "internal"
                , "(考勤|休假|报销|财务|人事|照片|绩效|培训|IT|办公|行政|固定资产|会议室|安全|工位|加班|审批流|差旅)"));
        routingRuleList.add(new RoutingRule(99, "pending_document"
                , ""));
        rules = routingRuleList;
        rules.sort(Comparator.comparingInt(RoutingRule::getPriority));
    }

    public static String matchTargetDb(String text) {
        if (Objects.isNull(text) || text.isEmpty()) {
            return null;
        }
        if (Objects.isNull(rules)) {
            initRules();
        }
        Optional<RoutingRule> ruleOptional = rules.stream().filter(rule -> rule.matches(text)).findFirst();
        return ruleOptional.map(RoutingRule::getTargetDb).orElse("");
    }


    public static class RoutingRule {
        @Getter
        private final int priority;//优先级
        @Getter
        private final String targetDb;//目标库
        private final Pattern pattern;//预编译的正则表达式

        public RoutingRule(int priority, String targetDb, String regex) {
            this.priority = priority;
            this.targetDb = targetDb;
            this.pattern = Pattern.compile(regex);
        }

        public boolean matches(String text) {
            return pattern.matcher(text).find();
        }
    }
}
