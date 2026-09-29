package com.QueroTrabalhar.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JWTUtilTest {

    @Test
    void deveAceitarSegredoAleatorioQueNaoEstaEmBase64() {
        JWTUtil jwtUtil = new JWTUtil();
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);
        ReflectionTestUtils.setField(
                jwtUtil,
                "jwtSecret",
                "3d957f31-7b55-4091-a75e-7ef0ef12068a43e9152e-8320-45fd-837d-78a7a0916c0e"
        );

        String token = jwtUtil.generateToken("usuario@teste.com");

        assertTrue(jwtUtil.validToken(token));
        assertEquals("usuario@teste.com", jwtUtil.getUserName(token));
    }
}
