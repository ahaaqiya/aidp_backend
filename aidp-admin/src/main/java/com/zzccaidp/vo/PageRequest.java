package com.zzccaidp.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 分页请求
 *
 * @author zhangtiantian
 * @date 2025/3/24
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
public class PageRequest<T> extends ReqHeader implements Serializable {

    private static final long serialVersionUID = 7340970524888689606L;

    /*
    页码
     */
    @NotNull
    private Integer pageNum = 1;

    /*
    分页大小
     */
    @NotNull
    @Min(0L)
    private Integer pageSize = 10;

    private T data;
}
