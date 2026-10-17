package uz.aytjanov.dropnest.messaging.jwt;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import uz.aytjanov.dropnest.security.JwtUtils;
import uz.aytjanov.dropnest.service.CustomUserDetailsService;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
  private final JwtUtils jwtUtils;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessageDeliveryException("Missing or malformed Authorization header");
        }
        String token =  header.substring(7);
        try {
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails user = customUserDetailsService.loadUserByUsername(username);

            if (!jwtUtils.validateToken(token)) {
                throw new MessageDeliveryException("Invalid or expired token");
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            accessor.setUser(auth);
        } catch (JwtException | UsernameNotFoundException e) {
            throw new MessageDeliveryException("Authentication failed");
        }

        return message;
    }
}
