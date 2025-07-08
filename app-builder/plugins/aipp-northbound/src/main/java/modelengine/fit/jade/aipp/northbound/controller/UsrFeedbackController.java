/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.fit.jade.aipp.northbound.controller;

import modelengine.fit.http.annotation.GetMapping;
import modelengine.fit.http.annotation.PatchMapping;
import modelengine.fit.http.annotation.PathVariable;
import modelengine.fit.http.annotation.PostMapping;
import modelengine.fit.http.annotation.RequestBody;
import modelengine.fit.http.annotation.RequestMapping;
import modelengine.fit.jane.common.controller.AbstractController;
import modelengine.fit.jane.task.gateway.Authenticator;
import modelengine.fitframework.annotation.Component;
import modelengine.jade.app.engine.base.dto.UsrFeedbackDto;
import modelengine.jade.app.engine.base.service.UsrFeedbackService;

/**
 * 用户反馈北向接口。
 *
 * @author 陈潇文
 * @since 2025-07-18
 */
@Component
@RequestMapping(path = "/v1/api/external/aipp/usr")
public class UsrFeedbackController extends AbstractController {
    private final UsrFeedbackService usrFeedbackService;

    public UsrFeedbackController(Authenticator authenticator, UsrFeedbackService usrFeedbackService) {
        super(authenticator);
        this.usrFeedbackService = usrFeedbackService;
    }

    /**
     * 创建用户反馈记录
     *
     * @param usrFeedbackDto 用户反馈消息体
     */
    @PostMapping("/feedback")
    public void createUsrFeedback(@RequestBody UsrFeedbackDto usrFeedbackDto) {
        usrFeedbackService.create(usrFeedbackDto);
    }

    /**
     * 更新用户反馈信息
     *
     * @param usrFeedbackDto 用户反馈消息体
     * @param instanceId 对话实例Id
     */
    @PatchMapping("/feedback/{instanceId}")
    public void updateUsrFeedback(@PathVariable("instanceId") String instanceId,
            @RequestBody UsrFeedbackDto usrFeedbackDto) {
        usrFeedbackService.updateOne(instanceId, usrFeedbackDto);
    }

    /**
     * 通过LogId获取对话信息列表
     *
     * @param instanceId 对话实例Id
     * @return 对话信息
     */
    @GetMapping("/feedback/{instanceId}")
    public UsrFeedbackDto getAllAnswerByInstanceId(@PathVariable("instanceId") String instanceId) {
        return usrFeedbackService.getUsrFeedbackByInstanceId(instanceId);
    }
}
