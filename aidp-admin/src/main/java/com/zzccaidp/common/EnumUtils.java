package com.zzccaidp.common;

import com.zzccaidp.common.BaseEnum;


/**
 * @author bades
 */
public class EnumUtils {

	private EnumUtils() {
	}

	public static <E, T extends BaseEnum<E, ?>> T getByVal(E o, T[] enums) {
		for (T anEnum : enums) {
			if (anEnum.getVal().equals(o)) {
				return anEnum;
			}
		}
		return null;
	}

	public static <E, T extends BaseEnum<E, X>, X> X getDesc(E value, T[] enums) {
		if (value != null) {
			T anEnum = getByVal(value, enums);
			if (anEnum != null) {
				return anEnum.getDesc();
			}
		}
		return null;
	}

}
