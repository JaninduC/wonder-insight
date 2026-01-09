package io.github.janinduc.model;

import lombok.Data;

import java.util.Date;

@Data
public class ServerDetailsVO {
    private Double cpuLoad;
    private Long totalRamMB;
    private Long usedRamMB;
    private Long freeRamMB;

    private String serverIp;
    private Double systemLoad;

    private Date time;
}
