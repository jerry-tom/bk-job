/*
 * Tencent is pleased to support the open source community by making BK-JOB蓝鲸智云作业平台 available.
 *
 * Copyright (C) 2021 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-JOB蓝鲸智云作业平台 is licensed under the MIT License.
 *
 * License for BK-JOB蓝鲸智云作业平台:
 * --------------------------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.tencent.bk.job.logsvr.api;

import com.tencent.bk.job.common.model.ServiceResponse;
import com.tencent.bk.job.common.model.dto.IpDTO;
import com.tencent.bk.job.logsvr.model.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 执行日志服务
 */
@Api(tags = {"Log"})
@RequestMapping("/service/log")
@RestController
public interface ServiceLogResource {

    @ApiOperation("保存执行日志")
    @PostMapping
    ServiceResponse saveLog(
        @ApiParam("保存日志请求报文")
        @RequestBody SaveLogRequest request);

    @ApiOperation("保存执行日志")
    @PostMapping("/batch")
    ServiceResponse saveLogs(
        @ApiParam("批量保存日志请求报文")
        @RequestBody BatchSaveLogRequest request);

    @ApiOperation("获取agent对应的执行日志")
    @GetMapping("/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}/ip/{ip}")
    ServiceResponse<ServiceLogDTO> getIpLogContent(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("ip")
        @PathVariable("ip") String ip,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate,
        @ApiParam("日志类型")
        @RequestParam("logType") Integer logType);

    @ApiOperation("获取脚本任务agent对应的执行日志")
    @GetMapping("/script/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}/ip/{ip}")
    ServiceResponse<ServiceLogDTO> getScriptIpLogContent(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("ip")
        @PathVariable("ip") String ip,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate);

    @ApiOperation("批量获取脚本任务agent对应的执行日志")
    @PostMapping("/script/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}")
    ServiceResponse<List<ServiceLogDTO>> batchGetScriptLogContent(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate,
        @ApiParam("查询请求")
        @RequestBody ScriptLogQueryRequest query);

    @ApiOperation("按照IP获取文件任务对应的执行日志")
    @GetMapping("/file/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}/ip/{ip}")
    ServiceResponse<ServiceLogDTO> getFileIpLogContent(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("ip")
        @PathVariable("ip") String ip,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate,
        @ApiParam("分发模式,0:upload,1:download")
        @RequestParam(value = "mode", required = false) Integer mode);

    @ApiOperation("获取文件任务对应的执行日志")
    @GetMapping("/file/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}")
    ServiceResponse<List<ServiceFileTaskLogDTO>> getFileLogContent(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate,
        @ApiParam("分发模式,0:upload,1:download")
        @RequestParam(value = "mode", required = false) Integer mode,
        @ApiParam("ip")
        @RequestParam(value = "ip", required = false) String ip);

    @ApiOperation("获取文件任务agent对应的执行日志")
    @PostMapping("/file/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}/queryByTaskIds")
    ServiceResponse<ServiceLogDTO> getFileLogContentListByTaskIds(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate,
        @ApiParam("文件任务ID列表，多个任务ID以;分隔")
        @RequestBody List<String> taskIds);

    @ApiOperation("删除执行日志")
    @DeleteMapping("/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}")
    ServiceResponse deleteStepContent(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate);

    /**
     * 返回日志内容包含关键字的主机ip
     *
     * @param stepInstanceId 步骤ID
     * @param executeCount   执行次数
     * @param jobCreateDate  创建时间
     * @param keyword        查询关键字
     * @return ip
     */
    @ApiOperation("根据脚本任务日志关键字获取对应的ip")
    @GetMapping("/keywordMatch/jobCreateDate/{jobCreateDate}/step/{stepInstanceId}/retry/{executeCount}")
    ServiceResponse<List<IpDTO>> getIpsByKeyword(
        @ApiParam("步骤ID")
        @PathVariable("stepInstanceId") Long stepInstanceId,
        @ApiParam("执行次数")
        @PathVariable("executeCount") Integer executeCount,
        @ApiParam("作业创建时间")
        @PathVariable("jobCreateDate") String jobCreateDate,
        @ApiParam("关键字")
        @RequestParam("keyword") String keyword);

}
