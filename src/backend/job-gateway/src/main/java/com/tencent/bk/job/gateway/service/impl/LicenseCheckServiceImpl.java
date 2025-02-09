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

package com.tencent.bk.job.gateway.service.impl;

import com.tencent.bk.job.common.constant.ErrorCode;
import com.tencent.bk.job.common.util.json.JsonUtils;
import com.tencent.bk.job.gateway.config.BkConfig;
import com.tencent.bk.job.gateway.model.LicenseCheckResultDTO;
import com.tencent.bk.job.gateway.service.LicenseCheckService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@SuppressWarnings("all")
public class LicenseCheckServiceImpl implements LicenseCheckService {

    private final BkConfig bkConfig;

    private CloseableHttpClient httpClient;

    private Thread checkLicenseThread;

    private boolean run = true;

    private volatile LicenseCheckResultDTO licenseCheckResult;

    public LicenseCheckServiceImpl(BkConfig bkConfig) {
        this.bkConfig = bkConfig;
    }

    @Override
    public LicenseCheckResultDTO checkLicense() {
        if (licenseCheckResult == null) {
            synchronized (LicenseCheckServiceImpl.class) {
                if (licenseCheckResult == null) {
                    licenseCheckResult = getLicenceCheckResult();
                }
            }
        }
        return licenseCheckResult;
    }


    @PreDestroy
    public void destroy() {
        run = false;
        checkLicenseThread.interrupt();
    }

    @PostConstruct
    public void init() throws Exception {
        if (httpClient == null) {
            X509TrustManager tm = new X509TrustManager() {
                @Override
                @SuppressWarnings("all")
                public void checkClientTrusted(X509Certificate[] xcs, String string) {
                }

                @Override
                @SuppressWarnings("all")
                public void checkServerTrusted(X509Certificate[] xcs, String string) {
                }

                @Override
                @SuppressWarnings("all")
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            @SuppressWarnings("all")
            HostnameVerifier hostnameVerifier = (arg0, arg1) -> true;

            try {
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(null, new TrustManager[]{tm}, null);

                httpClient = HttpClientBuilder.create()
                    .setDefaultConnectionConfig(
                        ConnectionConfig.custom()
                            .setBufferSize(8192)
                            .setCharset(org.apache.http.Consts.UTF_8)
                            .build())
                    .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(15000)
                        .setConnectTimeout(15000)
                        .setSocketTimeout(15000).build())
                    .setConnectionTimeToLive(180, TimeUnit.SECONDS)
                    .disableAutomaticRetries()
                    .disableAuthCaching()
                    .disableCookieManagement().setSSLContext(ctx)
                    .setSSLHostnameVerifier(hostnameVerifier).build();
            } catch (Exception e) {
                log.error("Init license check http client fail", e);
                throw e;
            }
            startCheckLicenceThread();
        }
    }

    private void startCheckLicenceThread() {
        if (checkLicenseThread == null || !checkLicenseThread.isAlive()) {
            checkLicenseThread = new Thread("Check-License") {

                @Override
                public void run() {
                    long minCheckInterval = 60 * 1000L;
                    long maxCheckInterval = 2 * 3600 * 1000L;
                    long sleepTime = minCheckInterval;
                    while (run) {
                        licenseCheckResult = getLicenceCheckResult();
                        if (licenseCheckResult != null && licenseCheckResult.isOk()) {
                            // 检查下次要再向LicenceServer通信的时间,根据有效期来定
                            if (licenseCheckResult.getValidEndTime() != null) {
                                sleepTime = licenseCheckResult.getValidEndTime().getTime() - System.currentTimeMillis();
                            }
                        } else { //错误情况下，每分钟都会尝试去连接验证Licence，以求最快速度恢复系统服务
                            sleepTime = minCheckInterval;
                        }

                        if (sleepTime > maxCheckInterval) {
                            sleepTime = maxCheckInterval;
                        }
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            };
            checkLicenseThread.start();
        }
    }


    private LicenseCheckResultDTO getLicenceCheckResult() {
        int maxRetryTimes = bkConfig.getLicenseCheckRetryTimes();
        int retryInterval = bkConfig.getLicenseCheckRetryInterval();

        int retryTimes = 0;
        LicenseCheckResultDTO licenseCheckResult = new LicenseCheckResultDTO();
        while (true) {
            try {
                HttpPost httpPost = new HttpPost(bkConfig.getLicenseCheckServiceUrl());
                httpPost.setHeader("Content-Type", "application/json");
                String requestBody = buildCheckRequestBody();
                httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

                String responseBody = httpClient.execute(httpPost, new BasicResponseHandler());
                log.info("Check license, resp={}", responseBody);
                if (StringUtils.isNotBlank(responseBody)) {
                    licenseCheckResult = JsonUtils.fromJson(responseBody, LicenseCheckResultDTO.class);
                } else {
                    createFailCheck(licenseCheckResult);
                }
                break;
            } catch (IOException e) {
                retryTimes++;
                if (retryTimes <= maxRetryTimes) {
                    log.warn("Exception while getting license! Retrying...|{}", retryTimes);
                    try {
                        Thread.sleep(1000 * retryInterval);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    createFailCheck(licenseCheckResult);
                    log.error("Check license retry exceed max-retry-times:{}!", maxRetryTimes);
                    break;
                }
            } catch (Throwable e) {
                createFailCheck(licenseCheckResult);
                log.error("Check license failed", e);
                break;
            }
        }
        //证书校验是否通过
        licenseCheckResult.setOk(licenseCheckResult.isStatus() && licenseCheckResult.getResult() == 0);
        return licenseCheckResult;
    }

    private void createFailCheck(LicenseCheckResultDTO licenseCheckDto) {
        licenseCheckDto.setStatus(true);
        licenseCheckDto.setMessage("License Server unreachable");
        licenseCheckDto.setResult(ErrorCode.RESULT_OK);
        licenseCheckDto.setValidStartTime(new Date());
        licenseCheckDto.setValidEndTime(new Date(DateTime.now().plusDays(100).getMillis()));
    }

    private String buildCheckRequestBody() throws IOException {
        String licenseFilePath = bkConfig.getLicenseFilePath();
        String certContent = FileUtils.readFileToString(new File(licenseFilePath), "UTF-8");

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("platform", "job");
        requestParams.put("time", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        requestParams.put("certificate", certContent);
        return JsonUtils.toJson(requestParams);
    }
}
