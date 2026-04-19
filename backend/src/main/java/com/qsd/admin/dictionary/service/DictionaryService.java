package com.qsd.admin.dictionary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.dictionary.dto.DictionaryGroupResponse;
import com.qsd.admin.dictionary.dto.DictionaryItemResponse;
import com.qsd.admin.dictionary.dto.DictionaryItemSaveRequest;
import com.qsd.admin.dictionary.dto.DictionaryOptionResponse;
import com.qsd.admin.dictionary.entity.DictionaryItem;
import com.qsd.admin.dictionary.mapper.DictionaryItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DictionaryItemMapper dictionaryItemMapper;

    public DictionaryService(DictionaryItemMapper dictionaryItemMapper) {
        this.dictionaryItemMapper = dictionaryItemMapper;
    }

    public List<DictionaryGroupResponse> listGroups() {
        List<DictionaryItem> items = dictionaryItemMapper.selectList(
            new LambdaQueryWrapper<DictionaryItem>()
                .orderByAsc(DictionaryItem::getDictType)
                .orderByAsc(DictionaryItem::getSortNo)
                .orderByAsc(DictionaryItem::getId)
        );

        Map<String, List<DictionaryItemResponse>> itemsByType = new LinkedHashMap<>();
        Map<String, String> namesByType = new LinkedHashMap<>();
        for (DictionaryItem item : items) {
            itemsByType.computeIfAbsent(item.getDictType(), key -> new ArrayList<>()).add(toResponse(item));
            namesByType.putIfAbsent(item.getDictType(), item.getDictName());
        }

        List<DictionaryGroupResponse> groups = new ArrayList<>();
        for (Map.Entry<String, List<DictionaryItemResponse>> entry : itemsByType.entrySet()) {
            groups.add(new DictionaryGroupResponse(entry.getKey(), namesByType.get(entry.getKey()), entry.getValue()));
        }
        return groups;
    }

    public Map<String, List<DictionaryOptionResponse>> listOptionsByTypes(List<String> types, boolean enabledOnly) {
        Map<String, List<DictionaryOptionResponse>> result = new LinkedHashMap<>();
        if (types == null || types.isEmpty()) {
            return result;
        }

        List<String> normalizedTypes = new ArrayList<>();
        for (String type : types) {
            String normalized = trimToNull(type);
            if (normalized == null || result.containsKey(normalized)) {
                continue;
            }
            result.put(normalized, new ArrayList<>());
            normalizedTypes.add(normalized);
        }
        if (normalizedTypes.isEmpty()) {
            return result;
        }

        LambdaQueryWrapper<DictionaryItem> query = new LambdaQueryWrapper<DictionaryItem>()
            .in(DictionaryItem::getDictType, normalizedTypes)
            .orderByAsc(DictionaryItem::getDictType)
            .orderByAsc(DictionaryItem::getSortNo)
            .orderByAsc(DictionaryItem::getId);
        if (enabledOnly) {
            query.eq(DictionaryItem::getEnabled, 1);
        }

        List<DictionaryItem> items = dictionaryItemMapper.selectList(query);
        for (DictionaryItem item : items) {
            result.computeIfAbsent(item.getDictType(), key -> new ArrayList<>())
                .add(new DictionaryOptionResponse(item.getItemLabel(), item.getItemValue()));
        }
        return result;
    }

    public String labelOf(String dictType, String itemValue) {
        String normalizedType = trimToNull(dictType);
        String normalizedValue = trimToNull(itemValue);
        if (normalizedType == null || normalizedValue == null) {
            return itemValue == null ? "" : itemValue;
        }

        DictionaryItem item = dictionaryItemMapper.selectOne(
            new LambdaQueryWrapper<DictionaryItem>()
                .eq(DictionaryItem::getDictType, normalizedType)
                .eq(DictionaryItem::getItemValue, normalizedValue)
                .eq(DictionaryItem::getEnabled, 1)
                .last("limit 1")
        );
        return item == null ? itemValue : item.getItemLabel();
    }

    @Transactional
    public DictionaryItemResponse create(DictionaryItemSaveRequest request) {
        String dictType = requireText(request.dictType(), "字典类型不能为空");
        String dictName = requireText(request.dictName(), "字典名称不能为空");
        String itemLabel = requireText(request.itemLabel(), "字典标签不能为空");
        String itemValue = requireText(request.itemValue(), "字典值不能为空");

        ensureValueAvailable(dictType, itemValue, null);

        DictionaryItem item = new DictionaryItem();
        item.setDictType(dictType);
        item.setDictName(dictName);
        item.setItemLabel(itemLabel);
        item.setItemValue(itemValue);
        item.setSortNo(request.sortNo());
        item.setEnabled(Boolean.TRUE.equals(request.enabled()) ? 1 : 0);
        item.setBuiltin(0);
        item.setRemark(trimToEmpty(request.remark()));
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        dictionaryItemMapper.insert(item);
        return toResponse(requireItem(item.getId()));
    }

    @Transactional
    public DictionaryItemResponse update(Long id, DictionaryItemSaveRequest request) {
        DictionaryItem item = requireItem(id);

        String dictType = requireText(request.dictType(), "字典类型不能为空");
        String itemValue = requireText(request.itemValue(), "字典值不能为空");
        if (item.getBuiltin() != null && item.getBuiltin() == 1) {
            dictType = item.getDictType();
            itemValue = item.getItemValue();
        } else {
            ensureValueAvailable(dictType, itemValue, id);
        }

        item.setDictType(dictType);
        item.setDictName(requireText(request.dictName(), "字典名称不能为空"));
        item.setItemLabel(requireText(request.itemLabel(), "字典标签不能为空"));
        item.setItemValue(itemValue);
        item.setSortNo(request.sortNo());
        item.setEnabled(Boolean.TRUE.equals(request.enabled()) ? 1 : 0);
        item.setRemark(trimToEmpty(request.remark()));
        item.setUpdatedAt(LocalDateTime.now());
        dictionaryItemMapper.updateById(item);
        return toResponse(requireItem(id));
    }

    @Transactional
    public void delete(Long id) {
        DictionaryItem item = requireItem(id);
        if (item.getBuiltin() != null && item.getBuiltin() == 1) {
            throw new IllegalArgumentException("内置字典项不允许删除");
        }
        dictionaryItemMapper.deleteById(id);
    }

    private void ensureValueAvailable(String dictType, String itemValue, Long currentId) {
        DictionaryItem existing = dictionaryItemMapper.selectOne(
            new LambdaQueryWrapper<DictionaryItem>()
                .eq(DictionaryItem::getDictType, dictType)
                .eq(DictionaryItem::getItemValue, itemValue)
                .last("limit 1")
        );
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new IllegalArgumentException("同一字典类型下的字典值已存在");
        }
    }

    private DictionaryItem requireItem(Long id) {
        DictionaryItem item = dictionaryItemMapper.selectById(id);
        if (item == null) {
            throw new NotFoundException("字典项不存在");
        }
        return item;
    }

    private DictionaryItemResponse toResponse(DictionaryItem item) {
        return new DictionaryItemResponse(
            item.getId(),
            item.getDictType(),
            item.getDictName(),
            item.getItemLabel(),
            item.getItemValue(),
            item.getSortNo(),
            item.getEnabled() != null && item.getEnabled() == 1,
            item.getBuiltin() != null && item.getBuiltin() == 1,
            item.getRemark(),
            formatDateTime(item.getUpdatedAt())
        );
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }
}
