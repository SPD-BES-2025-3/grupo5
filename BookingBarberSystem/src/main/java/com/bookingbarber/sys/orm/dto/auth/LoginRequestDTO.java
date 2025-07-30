package com.bookingbarber.sys.orm.dto.auth;

public record LoginRequestDTO(
        String email,
        String password
) {
}
