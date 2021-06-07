package com.alrayan.blackboxtester;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class LoadTestResult {
    private String name;
    private boolean passed;
    private long executionTime;
}
