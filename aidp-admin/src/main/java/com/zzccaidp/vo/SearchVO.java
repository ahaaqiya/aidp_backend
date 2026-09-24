package com.zzccaidp.vo;

import com.zzccaidp.enums.SearchSortEnum;
import com.github.pagehelper.IPage;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author bades
 */
@Data
@Accessors(chain = true)
public class SearchVO implements IPage, Serializable {

	protected Integer pageNum;
	protected Integer pageSize;
	protected String orderBy;
	protected String sort;

	public Integer getOffset() {
		if (this.pageNum != null && this.pageSize != null) {
			return (this.pageNum - 1) * this.pageSize;
		}
		return 0;
	}

	public void limit(int size, String orderBy, SearchSortEnum sortEnum) {
		this.pageNum = 1;
		this.pageSize = size;
		this.orderBy = StringUtils.isEmpty(orderBy) ? "id" : orderBy;
		this.sort = sortEnum == null ? SearchSortEnum.ASC.getValue() : sortEnum.getValue();
	}

}
