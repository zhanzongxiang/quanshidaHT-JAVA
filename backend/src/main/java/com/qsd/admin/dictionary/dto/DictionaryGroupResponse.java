package com.qsd.admin.dictionary.dto;

import java.util.List;

public record DictionaryGroupResponse(
    String dictType,
    String dictName,
    List<DictionaryItemResponse> items
) {
}
