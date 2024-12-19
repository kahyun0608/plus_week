package com.example.demo.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    @Test
    void encode() {
        //Given
        String rawPassword = "Password123!";
        //When
        String encodedPassword = BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, rawPassword.toCharArray());
        //Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
    }

    @Test
    void matches_success() {
        //Given
        String savedPassword = "Password123!";
        String encodedPassword = BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, savedPassword.toCharArray());
        String rawPassword = "Password123!";
        //When
        Boolean result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
        //Then
        assertThat(result).isTrue();

    }

    @Test
    void matches_fail() {
        //Given
        String savedPassword = "Password123!";
        String encodedPassword = BCrypt.withDefaults().hashToString(BCrypt.MIN_COST, savedPassword.toCharArray());
        String rawPassword = "Hello123!";
        //When
        Boolean result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
        //Then
        assertThat(result).isFalse();
    }
}