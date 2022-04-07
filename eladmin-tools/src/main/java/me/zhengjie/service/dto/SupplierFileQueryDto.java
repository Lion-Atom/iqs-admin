package me.zhengjie.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 9:26
 */
@Data
public class SupplierFileQueryDto {

    @NotNull
    private Long supplierId;

    @NotBlank
    private String fileType;

    private Long supplierContactId;

}
