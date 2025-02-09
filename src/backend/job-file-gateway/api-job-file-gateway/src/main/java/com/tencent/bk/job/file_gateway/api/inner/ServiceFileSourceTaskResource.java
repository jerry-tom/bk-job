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

package com.tencent.bk.job.file_gateway.api.inner;

import com.tencent.bk.job.common.model.ServiceResponse;
import com.tencent.bk.job.file_gateway.model.req.inner.*;
import com.tencent.bk.job.file_gateway.model.resp.inner.BatchTaskInfoDTO;
import com.tencent.bk.job.file_gateway.model.resp.inner.BatchTaskStatusDTO;
import com.tencent.bk.job.file_gateway.model.resp.inner.FileSourceTaskStatusDTO;
import com.tencent.bk.job.file_gateway.model.resp.inner.TaskInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;


@Api(tags = {"job-file-gateway:service:FileSourceTask"})
@RequestMapping("/service/fileSource/filetask")
@RestController
public interface ServiceFileSourceTaskResource {

    // 直接转发至FileWorker的请求，URL子路径保持一致
    @ApiOperation(value = "创建并启动文件下载任务", produces = "application/json")
    @PostMapping("/downloadFiles/start")
    ServiceResponse<TaskInfoDTO> startFileSourceDownloadTask(
        @ApiParam("用户名")
        @RequestHeader("username")
            String username,
        @ApiParam("文件源下载任务请求")
        @RequestBody FileSourceDownloadTaskReq req
    );

    @ApiOperation(value = "清理任务已下载的文件", produces = "application/json")
    @PostMapping("/downloadFiles/stop")
    ServiceResponse<Integer> stopTasks(
        @ApiParam("文件源下载任务请求")
        @RequestBody StopTaskReq req
    );

    @ApiOperation(value = "清理任务已下载的文件", produces = "application/json")
    @PostMapping("/clearFiles")
    ServiceResponse<Integer> clearTaskFiles(
        @ApiParam("文件源下载任务请求")
        @RequestBody ClearTaskFilesReq req
    );

    // 文件网关自有资源请求
    @ApiOperation(value = "获取文件任务状态", produces = "application/json")
    @GetMapping("/taskIds/{taskId}/status")
    ServiceResponse<FileSourceTaskStatusDTO> getFileSourceTaskStatusAndLogs(
        @ApiParam("任务Id")
        @PathVariable("taskId")
            String taskId,
        @ApiParam("日志开始位置")
        @RequestParam(value = "logStart", required = false)
            Long logStart,
        @ApiParam("获取日志数量")
        @RequestParam(value = "logLength", required = false)
            Long logLength
    );

    @ApiOperation(value = "创建并启动批量文件下载任务", produces = "application/json")
    @PostMapping("/batch/downloadFiles/start")
    ServiceResponse<BatchTaskInfoDTO> startFileSourceBatchDownloadTask(
        @ApiParam("用户名")
        @RequestHeader("username")
            String username,
        @ApiParam("文件源下载任务请求")
        @RequestBody FileSourceBatchDownloadTaskReq req
    );

    @ApiOperation(value = "清理批量任务已下载的文件", produces = "application/json")
    @PostMapping("/batch/downloadFiles/stop")
    ServiceResponse<Integer> stopBatchTasks(
        @ApiParam("文件源下载任务请求")
        @RequestBody StopBatchTaskReq req
    );

    @ApiOperation(value = "清理批量任务已下载的文件", produces = "application/json")
    @PostMapping("/batch/clearFiles")
    ServiceResponse<Integer> clearBatchTaskFiles(
        @ApiParam("文件源下载任务请求")
        @RequestBody ClearBatchTaskFilesReq req
    );

    // 文件网关自有资源请求
    @ApiOperation(value = "获取文件批量任务状态", produces = "application/json")
    @GetMapping("/batch/batchTaskIds/{batchTaskId}/status")
    ServiceResponse<BatchTaskStatusDTO> getBatchTaskStatusAndLogs(
        @ApiParam("任务Id")
        @PathVariable("batchTaskId")
            String batchTaskId,
        @ApiParam("日志开始位置")
        @RequestParam(value = "logStart", required = false)
            Long logStart,
        @ApiParam("获取日志数量")
        @RequestParam(value = "logLength", required = false)
            Long logLength
    );
}
