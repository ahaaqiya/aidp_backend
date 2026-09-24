package com.zzccaidp.vo.datasource;

import com.zzccaidp.vo.ReqHeader;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangtiantian
 * @date 2026/5/6
 */
@Data
public class CreateTableRequest extends ReqHeader {

    @NotBlank(message = "表名称不能为空")
    private String tableName;

    private String tableDescription;

    @NotBlank(message = "数据源Id不能为空")
    private String datasourceId;
}
