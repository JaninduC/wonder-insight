package com.wonder.insight.model;

import lombok.Data;

import java.util.Map;

@Data
public class DeveloperDetailsVO {

    private String returnType;          // return_type
    private String methodName;          // method_name
    private String errorId;             // error_id
    private Map<String, Object> params; // params
    private String className;           // class_name
    private String simpleClassName;     // simple_class_name
    private String moduleName;
}
