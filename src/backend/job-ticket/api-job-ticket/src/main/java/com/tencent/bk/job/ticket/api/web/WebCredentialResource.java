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

package com.tencent.bk.job.ticket.api.web;

import com.tencent.bk.job.common.model.PageData;
import com.tencent.bk.job.common.model.ServiceResponse;
import com.tencent.bk.job.ticket.model.web.req.CredentialCreateUpdateReq;
import com.tencent.bk.job.ticket.model.web.resp.CredentialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"job-ticket:web:Credential"})
@RequestMapping("/web/credentials/app/{appId}")
@RestController
public interface WebCredentialResource {

    @ApiOperation(value = "获取凭据列表", produces = "application/json")
    @GetMapping("/list")
    ServiceResponse<PageData<CredentialVO>> listCredentials(
        @ApiParam("用户名，网关自动传入")
        @RequestHeader("username")
            String username,
        @ApiParam(value = "业务ID", required = true)
        @PathVariable("appId") Long appId,
        @ApiParam("搜索条件：凭证ID")
        @RequestParam(value = "id", required = false)
            String id,
        @ApiParam("搜索条件：凭证名称")
        @RequestParam(value = "name", required = false)
            String name,
        @ApiParam("搜索条件：描述")
        @RequestParam(value = "description", required = false)
            String description,
        @ApiParam("搜索条件：创建人")
        @RequestParam(value = "creator", required = false)
            String creator,
        @ApiParam("搜索条件：更新人")
        @RequestParam(value = "lastModifyUser", required = false)
            String lastModifyUser,
        @ApiParam("分页-开始")
        @RequestParam(value = "start", required = false)
            Integer start,
        @ApiParam("分页-每页大小")
        @RequestParam(value = "pageSize", required = false)
            Integer pageSize);


    @ApiOperation(value = "新增/更新凭据", produces = "application/json")
    @PostMapping("")
    ServiceResponse<String> saveCredential(
        @ApiParam(value = "用户名，网关自动传入", required = true)
        @RequestHeader("username")
            String username,
        @ApiParam(value = "业务ID", required = true)
        @PathVariable("appId") Long appId,
        @ApiParam(value = "创建或更新请求体", required = true)
        @RequestBody
            CredentialCreateUpdateReq createUpdateReq
    );


    @ApiOperation(value = "删除凭据", produces = "application/json")
    @DeleteMapping("/ids/{id}")
    ServiceResponse<Integer> deleteCredentialById(
        @ApiParam("用户名，网关自动传入")
        @RequestHeader("username")
            String username,
        @ApiParam(value = "业务ID", required = true)
        @PathVariable("appId") Long appId,
        @ApiParam("凭据ID")
        @PathVariable("id")
            String id
    );
}
