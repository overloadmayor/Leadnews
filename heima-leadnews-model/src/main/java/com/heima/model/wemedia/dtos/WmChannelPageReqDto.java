package com.heima.model.wemedia.dtos;

import lombok.Data;

@Data
public class WmChannelPageReqDto {
    private String name;
    private Integer page;
    private Integer size;
}
