package com.qsd.admin.dictionary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DictionaryItemSaveRequest(
    @NotBlank(message = "字典类型不能为空")
    @Size(max = 64, message = "字典类型长度不能超过 64 个字符")
    String dictType,
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 64, message = "字典名称长度不能超过 64 个字符")
    String dictName,
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 64, message = "字典标签长度不能超过 64 个字符")
    String itemLabel,
    @NotBlank(message = "字典值不能为空")
    @Size(max = 64, message = "字典值长度不能超过 64 个字符")
    String itemValue,
    @NotNull(message = "排序号不能为空")
    Integer sortNo,
    @NotNull(message = "启用状态不能为空")
    Boolean enabled,
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    String remark
) {
}
