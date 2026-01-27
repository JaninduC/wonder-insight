package io.github.janinduc.handler;

import io.github.janinduc.LoghubClient;
import io.github.janinduc.config.ErrorTypeEnum;
import io.github.janinduc.exception.BaseException;
import io.github.janinduc.exception.TrackErrorBypassException;
import io.github.janinduc.model.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static io.github.janinduc.filter.TraceIdFilter.TRACE_ID;

/**
 * Global exception handler responsible for catching all unhandled exceptions
 * thrown by REST controllers and reporting them to LogHub.
 *
 * <p>
 * This handler enriches error details with trace ID, module, class, and method
 * information, sends error data asynchronously to the logging system,
 * and returns a standardized {@link AjaxResult} response to the client.
 * </p>
 *
 * <p>
 * Exceptions of type {@link TrackErrorBypassException} are excluded from
 * this global handling logic.
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerForLog {

    /**
     * Handles all uncaught {@link Exception} instances thrown by controller methods.
     *
     * <p>
     * This method:
     * <ul>
     *   <li>Extracts request trace ID and handler method metadata</li>
     *   <li>Sends detailed error information asynchronously to LogHub</li>
     *   <li>Builds a structured error response for the client</li>
     * </ul>
     * </p>
     *
     * @param ex            the exception thrown during request processing
     * @param req           the current HTTP servlet request
     * @param handlerMethod the controller method that caused the exception
     * @return an {@link AjaxResult} containing error details
     * @throws Throwable rethrows critical failures if required
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult<?> handleException(Exception ex, HttpServletRequest req, HandlerMethod handlerMethod) throws Throwable {
        String value = null;
        // Skip global handler if @TrackError handled the error

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

            Map<String, Object> requestData = new HashMap<>(map);

            // Send error details asynchronously to LogHub
            CompletableFuture.runAsync(() -> {
                try {
                    LoghubClient.sendError(
                            ex,
                            TRACE_ID,
                            req,
                            requestData,
                            ErrorTypeEnum.REGULAR_ERROR
                    ); // notify main LogHub
                } catch (Exception e) {
                    e.printStackTrace(); // avoid killing async thread silently
                }
            });

            // Remove internal metadata before sending response to client
            map.remove("module_name");
            map.remove("class_name");
            map.remove("simple_class_name");
            map.remove("method_name");

            if (ex.getClass().getSimpleName().equals("NotLoginException")) {
                return AjaxResult.fail(401, Objects.requireNonNull(ex.getMessage()), map);
            }

            if (ex instanceof BaseException be) {
                value = be.getCode();
                if (value != null) {
                    if (value.codePointCount(0, value.length()) > 99) {
                        return AjaxResult.fail("system.error" + value, map);
                    }
                    return AjaxResult.fail(value, map);
                }
            } else {
                if (ex.getMessage() != null) {
                    value = ex.getMessage();
                    if (value != null && value.codePointCount(0, value.length()) > 99) {
                        return AjaxResult.fail("system.error", map);
                    }
                    return AjaxResult.fail(ex.getMessage(), map);
                }
            }
            return AjaxResult.fail("system.error", map);
        }
        return AjaxResult.fail("system.error");
    }

    /**
     * Extracts the error code from a {@link BaseException} if applicable.
     *
     * @param ex the exception to inspect
     * @return the error code if the exception is a {@link BaseException},
     *         otherwise {@code null}
     */
    private String extractCode(Exception ex) {
        return (ex instanceof BaseException)
                ? ((BaseException) ex).getCode()
                : null;
    }

}
