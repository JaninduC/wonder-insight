package io.github.janinduc.model;

import lombok.Data;

import java.util.List;

@Data
public class ErrorReportVO {

    private List<ServerDetailsVO> serverDetailsSnap;

    private DeveloperDetailsVO developerDetails;

    private String title;
    private String traceId;
    private String exception;

}
