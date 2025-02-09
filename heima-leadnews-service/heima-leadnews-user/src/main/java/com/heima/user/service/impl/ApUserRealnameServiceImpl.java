package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.article.IArticleClient;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.common.constants.UserConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.users.dtos.AuthDto;
import com.heima.model.users.pojos.ApUser;
import com.heima.model.users.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import com.heima.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService  {
    @Autowired
    private ApUserService apUserService;

    @Autowired
    private IWemediaClient wemediaClient;

    @Override
    public ResponseResult list(AuthDto dto) {
        // 1. 检查参数
        if(dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2. 分页条件查询
        dto.checkParam();

        IPage page = new Page(dto.getPage(),dto.getSize());
        page=page(page,new LambdaQueryWrapper<ApUserRealname>()
                .eq(dto.getStatus()!=null, ApUserRealname::getStatus, dto.getStatus()));
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    @Override
    public ResponseResult updateByStatus(AuthDto dto, Short status) {
        if(dto == null || status == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUserRealname apUserRealname = new ApUserRealname();
        apUserRealname.setId(dto.getId());
        apUserRealname.setStatus(status);
        apUserRealname.setUpdatedTime(new Date());
        if(dto.getMsg()!=null){
            apUserRealname.setReason(dto.getMsg());
        }
        updateById(apUserRealname);
        if(status.equals(UserConstants.PASS_AUTH)){
            //通过审核
            ResponseResult responseResult = createNewWmUserAuthor(dto);
            if(responseResult!=null)return responseResult;
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    private ResponseResult createNewWmUserAuthor(AuthDto dto) {
        Integer UserRealnameId = dto.getId();
        ApUserRealname apUserRealname = getById(UserRealnameId);
        if(apUserRealname==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        ApUser apUser = apUserService.getById(UserRealnameId);
        if(apUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        WmUser wmUser = wemediaClient.findByName(apUser.getName());
        if(wmUser==null)
        {
            wmUser=new WmUser();
            wmUser.setName(apUser.getName());
            wmUser.setPassword(apUser.getPassword());
            wmUser.setCreatedTime(new Date());
            wmUser.setApUserId(apUser.getId());
            wmUser.setSalt(apUser.getSalt());
            wmUser.setPhone(apUser.getPhone());
            wmUser.setStatus(Integer.valueOf(UserConstants.PASS_AUTH));
            wemediaClient.save(wmUser);
        }
        apUser.setFlag((short)1);
        apUserService.updateById(apUser);
        return null;
    }
}
