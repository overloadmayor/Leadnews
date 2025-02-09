package com.heima.model.users.dtos;
 
import com.heima.model.common.annotation.IdEncrypt;
import lombok.Data;
 
/**
 * @author Z-熙玉
 * @version 1.0
 */
@Data
public class UserRelationDto {
 
    /*
     * 文章id
     */
    @IdEncrypt
    private Long article;
 
    /*
     * 作者id
     */
    @IdEncrypt
    private Integer authorId;
 
    /*
     * 0:关注 1:取消
     */
    private Short operation;
}