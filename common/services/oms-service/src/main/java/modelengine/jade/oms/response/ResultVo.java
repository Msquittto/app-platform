/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.oms.response;

import lombok.Builder;

/**
 * OMS 返回对象。
 *
 * @author 鲁为
 * @since 2024-11-19
 */
@Builder
public class ResultVo<T> {
    private static final String SUCCESS_CODE = "0";
    private static final String SUCCESS_MSG = "Success";

    /**
     * 状态码。
     */
    private String code;

    /**
     * 消息。
     */
    private String msg;

    /**
     * 数据。
     */
    private T data;

    public ResultVo() {
        this.code = SUCCESS_CODE;
        this.msg = SUCCESS_MSG;
    }

    public ResultVo(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultVo(T data) {
        this(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    /**
     * 获取状态码。
     *
     * @return 表示状态码的 {@link String}。
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置状态码。
     *
     * @param code 表示状态码的 {@link String}。
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取消息。
     *
     * @return 表示消息的 {@link String}。
     */
    public String getMsg() {
        return this.msg;
    }

    /**
     * 获取消息。
     *
     * @param msg 表示消息的 {@link String}。
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取数据。
     *
     * @return 表示数据的 {@link T}。
     */
    public T getData() {
        return this.data;
    }

    /**
     * 设置数据。
     *
     * @param data 表示数据的 {@link T}。
     */
    public void setData(T data) {
        this.data = data;
    }
}
