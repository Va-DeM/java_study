package org.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@ToString
public class Job {
    private LocalDate since;
    private String city;
    private BigDecimal salary;
}
