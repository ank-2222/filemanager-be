package com.file.manager.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenResponse {

    String access_token;
    int expires_in;
    String refresh_token;
    String scope;
    String token_type;
    String id_token;
}
