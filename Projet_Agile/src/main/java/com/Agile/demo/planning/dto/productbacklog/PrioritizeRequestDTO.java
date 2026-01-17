package com.Agile.demo.planning.dto.productbacklog;

import com.Agile.demo.model.PrioritizationMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrioritizeRequestDTO {
    private PrioritizationMethod method;
}