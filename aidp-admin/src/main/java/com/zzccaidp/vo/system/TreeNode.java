package com.zzccaidp.vo.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qiangzhuo.it on 2025/3/5.
 * tree的节点
 */
public class TreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;//id
    public String workCode;
    public String pid;//父id
    public String pidName;//父id
    public String label;//
    public boolean disabled;//
    public String icon; //小头像地址
    public String bigicon; //大头像地址
    public String pinyin; //首字母
    public String userId;
    List<TreeNode> children = new ArrayList<>();//子

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPidName() {
        return pidName;
    }

    public void setPidName(String pidName) {
        this.pidName = pidName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public TreeNode(String id, String label, String workCode, String pid, String pidName, boolean disabled, String icon, String bigicon) {
        this.id = id;
        this.label = label;
        this.workCode = workCode;
        this.pid = pid;
        this.pidName = pidName;
        this.disabled = disabled;
        this.icon = icon;
        this.bigicon = bigicon;
        this.children = new ArrayList<>();
    }

    public String getBigicon() {
        return bigicon;
    }

    public void setBigicon(String bigicon) {
        this.bigicon = bigicon;
    }

    public TreeNode(String id, String workCode, String label, String pid, String pidName, boolean disabled) {
        this.id = id;
        this.workCode = workCode;
        this.label = label;
        this.pid = pid;
        this.pidName = pidName;
        this.disabled = disabled;
        this.children = new ArrayList<>();
    }

    public TreeNode(String id, String workCode, String label, String pid, boolean disabled, String icon, String bigicon, String pinyin) {
        this.id = id;
        this.workCode = workCode;
        this.label = label;
        this.pid = pid;
        this.disabled = disabled;
        this.icon = icon;
        this.bigicon = bigicon;
        this.pinyin = pinyin;
        this.children = new ArrayList<>();
    }

    public TreeNode(String id, String workCode, String label, String pid, String pidName, boolean disabled, String icon, String bigicon, String pinyin, String userId) {
        this.id = id;
        this.workCode = workCode;
        this.label = label;
        this.pid = pid;
        this.pidName = pidName;
        this.disabled = disabled;
        this.icon = icon;
        this.bigicon = bigicon;
        this.pinyin = pinyin;
        this.userId = userId;
        this.children = new ArrayList<>();
    }

    public TreeNode(String id, String workCode, String label, String pid, String pidName, boolean disabled, String icon, String bigicon, String pinyin) {
        this.id = id;
        this.workCode = workCode;
        this.label = label;
        this.pid = pid;
        this.pidName = pidName;
        this.disabled = disabled;
        this.icon = icon;
        this.bigicon = bigicon;
        this.pinyin = pinyin;
        this.children = new ArrayList<>();
    }

    public TreeNode() {
    }
}
