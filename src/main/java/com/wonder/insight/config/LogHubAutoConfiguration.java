package com.wonder.insight.config;

import com.wonder.insight.LoghubClient;
import com.wonder.insight.filter.TraceIdFilter;
import com.wonder.insight.handler.GlobalExceptionHandlerForLog;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoghubProperties.class)
public class LogHubAutoConfiguration {
    private final boolean enabled;

    public LogHubAutoConfiguration(LoghubProperties properties) {
        validate(properties);
        this.enabled = properties.isEnabled();
    }

    private void validate(LoghubProperties props) {
        StringBuilder errors = new StringBuilder();
        if (props.isEnabled()) {
//            errors.append("wonder-insight.enabled is missing\n");
            if (props.getUrl() == null || props.getUrl().isEmpty()) {
                errors.append("wonder-insight.url is missing\n");
            }
            if (props.getToken() == null || props.getToken().isEmpty()) {
                errors.append("wonder-insight.token is missing\n");
            }
            if (!props.isEnabled()) {
                errors.append("wonder-insight.enabled is missing\n");
            }
        }


        if (errors.length() > 0) {
            throw new IllegalStateException(
                    "\n❌ wonder-insight configuration errors:\n" + errors
            );
        }
    }

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TraceIdFilter());
        bean.setOrder(1);
        return bean;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalExceptionHandlerForLog globalExceptionHandlerForLog() {
        System.out.println(" __        __                      _                             _                 _           _       _     \n" +
                " \\ \\      / /   ___    _ __     __| |   ___   _ __              (_)  _ __    ___  (_)   __ _  | |__   | |_   \n" +
                "  \\ \\ /\\ / /   / _ \\  | '_ \\   / _` |  / _ \\ | '__|    _____    | | | '_ \\  / __| | |  / _` | | '_ \\  | __|  \n" +
                "   \\ V  V /   | (_) | | | | | | (_| | |  __/ | |      |_____|   | | | | | | \\__ \\ | | | (_| | | | | | | |_   \n" +
                "    \\_/\\_/     \\___/  |_| |_|  \\__,_|  \\___| |_|                |_| |_| |_| |___/ |_|  \\__, | |_| |_|  \\__|  \n" +
                "                                                                                       |___/");

        if (!enabled) {

            System.out.println("wonder-insight deactivated  ❌");
            System.out.println("wonder-insight not-connected  ❌");
            System.out.println();
            System.out.println();
            return null;

        } else {
            System.out.println("wonder-insight activated  ✅");
            System.out.println("wonder-insight connected  ✅");
            System.out.println();
            System.out.println();
            return new GlobalExceptionHandlerForLog();
        }


    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoghubClient loghubClient(LoghubProperties props, RestTemplate restTemplate) {
        return new LoghubClient(props, restTemplate);
    }
}
