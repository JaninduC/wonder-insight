package com.wonder.insight.model;

import lombok.Data;

@Data
public class ErrorEvent {
    String error_id;
    String trace_id;
    String printTrackTrace;
    String uid;
    String timestamp;


}
