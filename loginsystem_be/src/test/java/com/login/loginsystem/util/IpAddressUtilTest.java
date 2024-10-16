package com.login.loginsystem.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpAddressUtilTest {

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
    }

    @Test
    void testGetClientIp_XForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.1", ip);
    }

    @Test
    void testGetClientIp_ProxyClientIP() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("192.168.1.2");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.2", ip);
    }

    @Test
    void testGetClientIp_WLProxyClientIP() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.168.1.3");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.3", ip);
    }

    @Test
    void testGetClientIp_HTTPClientIP() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn("192.168.1.4");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.4", ip);
    }

    @Test
    void testGetClientIp_HTTPXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("192.168.1.5");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.5", ip);
    }

    @Test
    void testGetClientIp_RemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.6");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.6", ip);
    }

    @Test
    void testGetClientIp_LocalhostIPv6() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("127.0.0.1", ip);
    }

    @Test
    void testGetClientIp_MultipleIPAddresses() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        String ip = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.1", ip);
    }

    @Test
    void testGetClientIp_NoHeadersAndRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(null);
        String ip = IpAddressUtil.getClientIp(request);
        assertNull(ip);
    }
}