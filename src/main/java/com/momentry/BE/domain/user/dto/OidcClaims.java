package com.momentry.BE.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OidcClaims {
    private String sub;
    private String email;
    private String name;
    private String picture;
    private Map<String, Object> rawClaims;

    public OidcClaims(Map<String, Object> rawClaims) {
        this.sub = (String) rawClaims.get("sub");
        this.email = (String) rawClaims.get("email");
        this.name = (String) rawClaims.get("name");
        this.picture = (String) rawClaims.get("picture");
        this.rawClaims = rawClaims;
    }
}
