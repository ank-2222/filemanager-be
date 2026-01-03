package com.file.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse<K,V> {
    private String message;
    private String message_code;
    private String error;
    private int status;
    private Map<K,V> data;  // Generic list

    // If you want data optional, you can overload constructors or pass null
}
