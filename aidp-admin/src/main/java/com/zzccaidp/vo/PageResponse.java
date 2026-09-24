package com.zzccaidp.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 公共分页结果集
 *
 * @author zhangtiantian
 * @date 2025/03/21
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
public class PageResponse<T> extends ResHeader implements Serializable {

    private static final long serialVersionUID = -6722427910383947796L;

    /*
    结果集
     */
    private List<T> records;

    /*
    当前页
     */
    private Integer pageNum;

    /*
    分页大小
     */
    private Integer pageSize;

    /*
    总页数
     */
    private Integer totalPage;

    /*
     总数
     */
    private Long totalCount;
}
