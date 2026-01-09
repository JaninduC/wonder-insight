package io.github.janinduc.handler;

import io.github.janinduc.LoghubClient;
import io.github.janinduc.config.ErrorTypeEnum;
import io.github.janinduc.exception.TrackErrorBypassException;
import io.github.janinduc.model.AjaxResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.janinduc.filter.TraceIdFilter.TRACE_ID;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandlerForLog {

    @ExceptionHandler(Exception.class)
    public AjaxResult<?> handleException(Exception ex,
                                         HttpServletRequest req,
                                         HandlerMethod handlerMethod) throws Throwable {
        if (!(ex instanceof TrackErrorBypassException)) {
            System.out.println("TraceId: " + req.getAttribute(TRACE_ID));
            ex.printStackTrace();
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
            Map<String,String> returnData=new HashMap<>();
            LoghubClient.sendError(ex, TRACE_ID, req, map, ErrorTypeEnum.REGULAR_ERROR); // notify main LogHub
            map.remove("module_name");
            map.remove("class_name");
            map.remove("simple_class_name");
            map.remove("method_name");

            returnData.put("error_id", req.getAttribute(TRACE_ID)+"");
            if (ex.getClass().getSimpleName().equals("XhadminException")) {
                //logger.error(e.getMessage());
                return AjaxResult.fail(501, Objects.requireNonNull(ex.getMessage()),returnData);
            }
            if (ex.getClass().getSimpleName().equals("TokenException")) {
                //logger.error(e.getMessage());
                return AjaxResult.fail(401, Objects.requireNonNull(ex.getMessage()),returnData);
            }
            if (ex.getClass().getSimpleName().equals("MissingServletRequestParameterException")) {
                System.out.println("formatFailed4");
                return AjaxResult.fail(400, Objects.requireNonNull(ex.getMessage()),returnData);
            }

            return AjaxResult.fail("system.error ", map);
        }
        return AjaxResult.fail("system.error ");
    }


}
