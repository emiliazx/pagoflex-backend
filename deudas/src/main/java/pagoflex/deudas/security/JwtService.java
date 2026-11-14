package pagoflex.deudas.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String extraerCorreo(String token) {
        return leer(token).getSubject();
    }

    public Long extraerId(String token) {
        Object valor = leer(token).get("id");
        if (valor instanceof Number numero) {
            return numero.longValue();
        }
        return null;
    }

    public String extraerRol(String token) {
        return leer(token).get("rol", String.class);
    }

    public boolean esValido(String token) {
        try {
            return leer(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
