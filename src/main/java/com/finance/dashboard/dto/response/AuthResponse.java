package com.finance.dashboard.dto.response;

import com.finance.dashboard.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String email;
    private Role role;
    @Builder.Default
    private String tokenType = "Bearer";
}
