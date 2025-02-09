package com.heima.model.wemedia.dtos;

import lombok.Data;

@Data
public class WmSensitivePageReqDto {
    private String name;
    private Integer page;
    private Integer size;
}
