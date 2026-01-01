package com.wonder.insight;


import com.sun.management.OperatingSystemMXBean;
import com.wonder.insight.config.ErrorTypeEnum;
import com.wonder.insight.config.LoghubProperties;
import com.wonder.insight.model.DeveloperDetailsVO;
import com.wonder.insight.model.ErrorReportVO;
import com.wonder.insight.model.ServerDetailsVO;
import com.wonder.insight.util.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Component
public class LoghubClient {

    private static LoghubClient instance;

    private final LoghubProperties loghubProperties;
    private final RestTemplate restTemplate;

    // Spring injects these through constructor injection
    public LoghubClient(LoghubProperties loghubProperties, RestTemplate restTemplate) {
        this.loghubProperties = loghubProperties;
        this.restTemplate = restTemplate;
        instance = this; // allows static access
    }

    // Static method but uses the injected bean instance
    public static void sendError(Exception ex, String traceId, HttpServletRequest req, Map<String, Object> map, ErrorTypeEnum errorTypeEnum) {
        try {
            if (instance == null) {
                throw new IllegalStateException("LoghubClient is not initialized by Spring!");
            } else if (instance.loghubProperties.isEnabled()) {

                ErrorReportVO payload = createPayLoad(traceId, ex, map);

                String url = instance.loghubProperties.getUrl() +
                        (errorTypeEnum == ErrorTypeEnum.REGULAR_ERROR ? "/insight/reportError/regular" : "/insight/reportError/job");

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Client-Token", instance.loghubProperties.getToken());
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<ErrorReportVO> entity = new HttpEntity<>(payload, headers);

                // Use String.class for response to avoid JSON parse errors
                ResponseEntity<String> exchange = instance.restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                if (exchange.getStatusCodeValue() != 200) {
                    System.out.println("Send fail: " + exchange.getBody());
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static ErrorReportVO createPayLoad(String traceId, Exception ex, Map<String, Object> map) {
        DeveloperDetailsVO dev = new DeveloperDetailsVO();
        if (map != null) {
            dev.setReturnType(map.get("return_type") != null ? map.get("return_type").toString() : null);
            dev.setMethodName(map.get("method_name") != null ? map.get("method_name").toString() : null);
            dev.setErrorId(map.get("error_id") != null ? map.get("error_id").toString() : null);
            dev.setParams(map.get("params") != null && map.get("params") instanceof Map ? (Map<String, Object>) map.get("params") : null);
            dev.setClassName(map.get("class_name") != null ? map.get("class_name").toString() : null);
            dev.setSimpleClassName(map.get("simple_class_name") != null ? map.get("simple_class_name").toString() : null);
            dev.setModuleName(map.get("module_name") != null ? map.get("module_name").toString() : null);
        }
        ErrorReportVO payload = new ErrorReportVO();
        payload.setDeveloperDetails(dev);
        payload.setServerDetailsSnap(Arrays.asList(getSystemDetails()));
        payload.setTitle(ex.getMessage());
        payload.setTraceId(traceId);
        payload.setException(ExceptionUtil.getStackTrace(ex));

        return payload;
    }

    public static ServerDetailsVO getSystemDetails() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long total = osBean.getTotalPhysicalMemorySize();
        long free = osBean.getFreePhysicalMemorySize();
        long used = total - free;

        ServerDetailsVO vo = new ServerDetailsVO();
        vo.setCpuLoad(osBean.getSystemCpuLoad() * 100.0);
        vo.setTotalRamMB(total / 1024 / 1024);
        vo.setUsedRamMB(used / 1024 / 1024);
        vo.setFreeRamMB(free / 1024 / 1024);
        vo.setServerIp(getRealIp());
        vo.setSystemLoad(osBean.getSystemLoadAverage());
        vo.setTime(new Date());

        return vo;
    }


    public static String getRealIp() {
        try {
            return new RestTemplate().getForObject("https://api.ipify.org", String.class);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
