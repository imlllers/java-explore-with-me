package ru.practicum.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStatDto {
    private String app;

    private String uri;

    private Long hits;
}