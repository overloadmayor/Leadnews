package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmSensitivePageReqDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WmSensitiveImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive> implements WmSensitiveService {
    @Override
    public ResponseResult delWords(Integer id) {
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult listWords(WmSensitivePageReqDto dto) {
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        IPage<WmSensitive> page=new Page<>(dto.getPage(),dto.getSize());
        page = page(page, new LambdaQueryWrapper<WmSensitive>()
                .like(WmSensitive::getSensitives, "%" + dto.getName() + "%")
                .orderByDesc(WmSensitive::getCreatedTime));

        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(),
                dto.getSize(),
                (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    @Override
    public ResponseResult saveWords(WmSensitive sensitiveWord) {
        sensitiveWord.setCreatedTime(new Date());
        WmSensitive wmSensitive = getOne(new LambdaQueryWrapper<WmSensitive>()
                .eq(WmSensitive::getSensitives, sensitiveWord.getSensitives()));
        if(wmSensitive != null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"敏感词已存在");
        }
        wmSensitive.setCreatedTime(new Date());
        save(sensitiveWord);
        return ResponseResult.okResult(sensitiveWord);
    }

    @Override
    public ResponseResult updateWords(WmSensitive sensitiveWord) {
        if(sensitiveWord.getSensitives()==null||sensitiveWord.getSensitives()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        updateById(sensitiveWord);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
