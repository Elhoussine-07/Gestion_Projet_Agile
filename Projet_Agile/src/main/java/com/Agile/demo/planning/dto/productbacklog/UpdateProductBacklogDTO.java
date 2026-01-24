package com.Agile.demo.planning.dto.productbacklog;

import com.Agile.demo.model.PrioritizationMethod;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductBacklogDTO {

    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    private PrioritizationMethod selectedMethod;
}