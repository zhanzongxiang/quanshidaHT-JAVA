package com.qsd.admin.dictionary.dto;

public record DictionaryItemResponse(
    Long id,
    String dictType,
    String dictName,
    String itemLabel,
    String itemValue,
    Integer sortNo,
    boolean enabled,
    boolean builtin,
    String remark,
    String updatedAt
) {
}
