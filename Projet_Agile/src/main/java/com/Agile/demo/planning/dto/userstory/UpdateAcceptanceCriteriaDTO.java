package com.Agile.demo.planning.dto.userstory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAcceptanceCriteriaDTO {
    private List<String> givenClauses;
    private List<String> whenClauses;
    private List<String> thenClauses;
}