package org.example.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDto<T> {

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_items")
    private Long totalItems;

    @JsonProperty("content")
    private List<T> content;
}
