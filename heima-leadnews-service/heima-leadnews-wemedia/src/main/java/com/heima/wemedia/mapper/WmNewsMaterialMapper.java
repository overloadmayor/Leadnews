package com.heima.wemedia.mapper;

import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 自媒体图文引用素材信息表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-10-12
 */
@Mapper
public interface WmNewsMaterialMapper extends BaseMapper<WmNewsMaterial> {
    void saveRelations(@Param("materialIds") List<Integer> materialIds,
                       @Param("newsId") Integer newsId,@Param("type")Short type);
}
