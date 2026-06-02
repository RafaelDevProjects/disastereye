package br.com.fiap.disastereye.service;

import br.com.fiap.disastereye.dto.response.ApiResponse.UserResponse;
import br.com.fiap.disastereye.dto.response.PageResponse;
import br.com.fiap.disastereye.exception.BusinessException;
import br.com.fiap.disastereye.exception.ResourceNotFoundException;
import br.com.fiap.disastereye.model.User;
import br.com.fiap.disastereye.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public PageResponse<UserResponse> listAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        return PageResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }

    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

    @Transactional
    public UserResponse updateRole(Long id, User.Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        user.setRole(role);
        log.info("Role do usuário [id={}] alterado para {}", id, role);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        user.setActive(!user.isActive());
        log.info("Usuário [id={}] {} ", id, user.isActive() ? "ativado" : "desativado");
        return UserResponse.from(userRepository.save(user));
    }
}
