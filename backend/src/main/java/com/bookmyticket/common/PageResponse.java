package com.bookmyticket.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> items, int page, int size, long total, boolean hasNext) {

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long total) {
        return new PageResponse<>(items, page, size, total, (long) (page + 1) * size < total);
    }

    public static <T> PageResponse<T> single(List<T> items) {
        return new PageResponse<>(items, 0, items.size(), items.size(), false);
    }

    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.hasNext());
    }
}
