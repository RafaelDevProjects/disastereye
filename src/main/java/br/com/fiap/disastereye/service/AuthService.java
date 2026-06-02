package br.com.fiap.disastereye.service;

import br.com.fiap.disastereye.dto.request.AuthRequest.LoginRequest;
import br.com.fiap.disastereye.dto.request.AuthRequest.RegisterRequest;
import br.com.fiap.disastereye.dto.response.ApiResponse.AuthResponse;
import br.com.fiap.disastereye.exception.BusinessException;
import br.com.fiap.disastereye.exception.ConflictException;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.repository.UserRepository;
import br.com.fiap.disastereye.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("email", request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .active(true)
                .build();

        userRepository.save(user);
        log.info("Novo usuário registrado: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user, jwtService.getExpirationDateTime(token));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String token = jwtService.generateToken(user);
        log.info("Login realizado: {}", user.getEmail());
        return AuthResponse.of(token, user, jwtService.getExpirationDateTime(token));
    }
}
