package com.wonder.insight.handler;

import com.wonder.insight.LoghubClient;
import com.wonder.insight.config.ErrorTypeEnum;
import com.wonder.insight.exception.TrackErrorBypassException;
import com.wonder.insight.model.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.wonder.insight.filter.TraceIdFilter.TRACE_ID;

@RestControllerAdvice
public class GlobalExceptionHandlerForLog {

    @ExceptionHandler(Exception.class)
    public AjaxResult<?> handleException(Exception ex, HttpServletRequest req, HandlerMethod handlerMethod) throws Throwable {

        // Skip global handler if @TrackError handled the error
        if (!(ex instanceof TrackErrorBypassException)) {
            Map<String, Object> map = new HashMap<>();
            map.put("error_id", req.getAttribute(TRACE_ID));
            // Method details
            Method method = handlerMethod.getMethod();
            String methodName = method.getName();
            String className = handlerMethod.getBeanType().getName();
            String simpleClassName = handlerMethod.getBeanType().getSimpleName();
            String moduleName = handlerMethod.getBeanType().getPackage().getName();

            map.put("module_name", moduleName);
            map.put("class_name", className);
            map.put("method_name", methodName);
            map.put("simple_class_name", simpleClassName);

            LoghubClient.sendError(ex, TRACE_ID, req, map, ErrorTypeEnum.REGULAR_ERROR); // notify main LogHub
            map.remove("module_name");
            map.remove("class_name");
            map.remove("simple_class_name");
            map.remove("method_name");
            return AjaxResult.fail("system.error ", map);

        }
        return AjaxResult.fail("system.error ");
    }


}
