package com.example.ecomerseapplication.Auth;

import com.example.ecomerseapplication.enums.UserRole;
import org.springframework.stereotype.Component;

@Component("roles")
public class RoleExpressions {
    public String admin() {
        return UserRole.ADMIN.getValue();
    }
    public String customer() {
        return UserRole.CUSTOMER.getValue();
    }
}