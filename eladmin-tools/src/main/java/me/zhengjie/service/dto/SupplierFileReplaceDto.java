package me.zhengjie.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/7/28 9:26
 */
@Data
public class SupplierFileReplaceDto {

    @NotNull
    private Long supplierId;

    @NotNull
    private Long uId;

    private Long supplierContactId;

    @NotEmpty
    private String [] fileTypes;

}
