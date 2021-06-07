package com.alrayan.blackboxtester;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class Expected {
    private int httpStatus;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> contains = new ArrayList<>();
}
