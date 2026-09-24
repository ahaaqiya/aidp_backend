package com.zzccaidp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bades
 */

@Getter
@AllArgsConstructor
public enum SearchSortEnum {

    //升序
    ASC(0, "ascending"),
    //降序
    DESC(1, "descending"),
    ;

    private final Integer code;
    private final String value;

}
