package com.zzccaidp.util;

import com.zzccaidp.enums.ErrCodeEnum;
import com.zzccaidp.exception.BusinessException;
import com.zzccaidp.vo.PageResponse;
import com.zzccaidp.vo.ResHeader;
import com.github.pagehelper.Page;
import lombok.extern.log4j.Log4j2;

/**
 * @author bades
 */
@Log4j2
public class ResUtils {

	private ResUtils() {
	}

	/**
	 * 构建成功响应
	 *
	 * @return ResHeader
	 */
	public static <T extends ResHeader> T success(T response) {
		response.setResResult(ErrCodeEnum.SUCCESS.getErrCode(), ErrCodeEnum.SUCCESS.getErrMsg());
		return response;
	}

	/**
	 * 构建成功响应
	 *
	 * @return ResHeader
	 */
	public static <T extends ResHeader> T success(Class<T> returnType) {
		return buildRes(returnType, ErrCodeEnum.SUCCESS.getErrCode(), ErrCodeEnum.SUCCESS.getErrMsg());
	}



	/**
	 * 构建系统错误响应
	 *
	 * @param response 响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T sysError(T response) {
		return error(response, ErrCodeEnum.ERROR);
	}

	/**
	 * 构建系统错误响应
	 *
	 * @param returnType 响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T sysError(Class<T> returnType) {
		return error(returnType, ErrCodeEnum.ERROR);
	}

	/**
	 * 构建失败响应
	 *
	 * @param errCode  异常码
	 * @param errMsg   errMsg
	 * @param response 响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T error(T response, String errCode, String errMsg) {
		response.setResResult(errCode, errCode);
		return response;
	}

	/**
	 * 构建失败响应
	 *
	 * @param errorCodeEnum 异常信息
	 * @param response      响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T error(T response, ErrCodeEnum errorCodeEnum) {
		return error(response, errorCodeEnum.getErrCode(), errorCodeEnum.getErrMsg());
	}

	/**
	 * 构建失败响应
	 *
	 * @param businessException 异常
	 * @param response          响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T error(T response, BusinessException businessException) {
		return error(response, businessException.getErrCode(), businessException.getMessage());
	}

	/**
	 * 构建失败响应
	 *
	 * @param businessException 异常
	 * @param returnType        响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T error(Class<T> returnType, BusinessException businessException) {
		return buildRes(returnType, businessException.getErrCode(), businessException.getMessage());
	}

	/**
	 * 构建失败响应
	 *
	 * @param errorCodeEnum 异常信息
	 * @param returnType    响应类型 @notNUll
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T error(Class<T> returnType, ErrCodeEnum errorCodeEnum) {
		return buildRes(returnType, errorCodeEnum.getErrCode(), errorCodeEnum.getErrMsg());
	}



	/**
	 * 构建失败响应
	 *
	 * @param returnType 返回类型
	 * @param errCode    错误码
	 * @param errMsg     错误信息
	 * @return extends ResHeader
	 */
	public static <T extends ResHeader> T buildRes(Class<T> returnType, String errCode, String errMsg) {
		if (returnType == null) {
			return null;
		} else {
			try {
				T response = returnType.newInstance();
				return error(response, errCode, errMsg);
			} catch (IllegalAccessException | InstantiationException e) {
				log.error("buildResponse error", e);
				return null;
			}
		}
	}

	/**
	 * 构建分页响应
	 *
	 * @param pageRes    分页数据
	 * @return extends ResPageHeader
	 */
	public static <T> PageResponse<T> buildPageRes(Class<T> pageType, Page<?> pageRes) {
		PageResponse<T> pageResponse = new PageResponse<>();
		pageResponse.setPageSize(pageRes.getPageSize());
		pageResponse.setPageNum(pageRes.getPageNum());
		pageResponse.setTotalCount(pageRes.getTotal());
		pageResponse.setTotalPage(pageRes.getPages());
		pageResponse.setSuccessCode();
		return pageResponse;
	}
}
