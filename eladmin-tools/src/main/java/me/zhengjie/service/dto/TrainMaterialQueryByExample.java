package me.zhengjie.service.dto;

import lombok.Data;
import me.zhengjie.annotation.Query;

import java.util.HashSet;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/6/10 13:28
 */
@Data
public class TrainMaterialQueryByExample {

    @Query(propName = "departId", type = Query.Type.IN)
    private Set<Long> departIds = new HashSet<>();
}
