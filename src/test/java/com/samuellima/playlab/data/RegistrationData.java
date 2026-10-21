package com.samuellima.playlab.data;

public record RegistrationData(String fullName,
                               String email,
                               String password,
                               String phone,
                               String bio,
                               String countryCode) {
}
