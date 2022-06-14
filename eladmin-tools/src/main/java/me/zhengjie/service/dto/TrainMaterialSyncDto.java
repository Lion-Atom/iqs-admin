package me.zhengjie.service.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/6/10 13:28
 */
@Data
public class TrainMaterialSyncDto {

    private Long trScheduleId;

    private String fileType;

    private Set<Long> bindingFileIds = new HashSet<>();
}
