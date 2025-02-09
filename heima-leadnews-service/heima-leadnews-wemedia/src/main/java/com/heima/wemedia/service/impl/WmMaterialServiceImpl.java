package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMeterialDTO;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile)  {
        if(multipartFile==null||multipartFile.getSize()==0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        String fileName= UUID.randomUUID().toString().replace("-","");
        String originalFilename = multipartFile.getOriginalFilename();
        int temp=originalFilename.lastIndexOf(".");
        String postfix="";
        if(temp>=0)
            postfix=fileName.substring(temp);
        String fileId=null;
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix,
                    multipartFile.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}",fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("上传文件失败");
        }
        //保存到数据库中
        WmMaterial wmMaterial = WmMaterial.builder()
                .userId(WmThreadLocalUtil.getUser().getId())
                .url(fileId)
                .isCollection((short) 0)
                .type((short) 0)
                .createdTime(new Date())
                .build();
        save(wmMaterial);
        //返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList(WmMeterialDTO wmMeterialDTO) {
        //检查参数
        wmMeterialDTO.checkParam();
        //分页查询
        IPage page=new Page(wmMeterialDTO.getPage(), wmMeterialDTO.getSize());
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //是否收藏
        if(wmMeterialDTO.getIsCollection()!=null&&wmMeterialDTO.getIsCollection()==1){
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection,wmMeterialDTO.getIsCollection());
        }
        System.out.println(WmThreadLocalUtil.getUser().getId());
        lambdaQueryWrapper.eq(WmMaterial::getUserId,WmThreadLocalUtil.getUser().getId());
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);

        page = page(page, lambdaQueryWrapper);
        PageResponseResult pageResponseResult = new PageResponseResult(wmMeterialDTO.getPage(), wmMeterialDTO.getSize(),
                (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }
}
