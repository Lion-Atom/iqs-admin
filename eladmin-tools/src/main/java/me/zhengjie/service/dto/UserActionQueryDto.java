package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/6/1 9:52
 */
@Data
public class UserActionQueryDto {

    /**
     * 措施名称
     */
    @Query(blurry = "name")
    private String blurry;

    @Query
    private String status;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}
