package com.samuellima.playlab.data;

public record RegistrationData(String fullName,
                               String email,
                               String password,
                               String phone,
                               String bio,
                               String countryCode) {

    public RegistrationData withEmail(String outroEmail) {
        return new RegistrationData(fullName, outroEmail, password, phone, bio, countryCode);
    }
}
