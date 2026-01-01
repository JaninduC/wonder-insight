package com.wonder.insight.annotation;


import com.wonder.insight.LoghubClient;
import com.wonder.insight.config.ErrorTypeEnum;
import com.wonder.insight.exception.TrackErrorBypassException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.wonder.insight.util.ExceptionUtil.buildParamMap;

@Aspect
@Component
public class JobErrorTrackAspect {
    private static final Logger log = LoggerFactory.getLogger(JobErrorTrackAspect.class);

    @Around("@annotation(trackError)")
    public Object track(ProceedingJoinPoint pjp, JobErrorTrack trackError) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        String simpleClassName = method.getDeclaringClass().getSimpleName();
        String[] paramNames = signature.getParameterNames();
        Object[] paramValues = pjp.getArgs();
        String returnType = signature.getReturnType().getSimpleName();

        String errorId = UUID.randomUUID().toString();

        try {
            return pjp.proceed();
        } catch (Exception ex) {

            log.error("Tracked Error in {}: {}",
                    pjp.getSignature().toShortString(),
                    ex.getMessage(), ex);

            Map<String, Object> map = new HashMap<>();
            map.put("class_name", className);
            map.put("method_name", methodName);
            map.put("return_type", returnType);
            map.put("simple_class_name", simpleClassName);
            map.put("params", buildParamMap(paramNames, paramValues));
            map.put("error_id", errorId);

            LoghubClient.sendError(ex, errorId, null, map, ErrorTypeEnum.JOB_ERROR);

            throw new TrackErrorBypassException(ex);
        }
    }

}
