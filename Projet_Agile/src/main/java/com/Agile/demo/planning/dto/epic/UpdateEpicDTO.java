package com.Agile.demo.planning.dto.epic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEpicDTO {
    private String title;
    private String description;
}