package com.example.cinema.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/internal/gateway")
public class GatewayInternalController {

    private final DynamicCorsConfiguration dynamicCorsConfiguration;
    private final DynamicSecurityConfiguration dynamicSecurityConfiguration;

    @Value("${APP_SECURITY_INTERNAL_API_KEY}")
    private String internalApiKey;

    public GatewayInternalController(DynamicCorsConfiguration dynamicCorsConfiguration,
                                     DynamicSecurityConfiguration dynamicSecurityConfiguration) {
        this.dynamicCorsConfiguration = dynamicCorsConfiguration;
        this.dynamicSecurityConfiguration = dynamicSecurityConfiguration;
    }

    @PostMapping("/refresh-cors")
    public Mono<Void> refreshCors(ServerWebExchange exchange) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-Internal-Api-Key");
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        dynamicCorsConfiguration.refreshCorsConfig();
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return exchange.getResponse().setComplete();
    }

    @PostMapping("/refresh-security")
    public Mono<Void> refreshSecurity(ServerWebExchange exchange) {
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-Internal-Api-Key");
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        dynamicSecurityConfiguration.refreshSecurityConfig();
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return exchange.getResponse().setComplete();
    }
}
