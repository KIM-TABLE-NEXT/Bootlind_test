package com.sparta.bootlind.service;

import com.sparta.bootlind.dto.requestDto.SignupRequest;
import com.sparta.bootlind.entity.User;
import com.sparta.bootlind.entity.UserRoleEnum;
import com.sparta.bootlind.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String ADMIN_TOKEN = "f679d89c320cc4adb72b7647a64ccbe520406dc3ee4578b44bcffbfa7ebbb85e30b964306b6398d3a2d7098ecd1bc203551e356ac5ec4a5ee0c7dc899fb704c5";

    public void signup(SignupRequest requestDto) {
        String username = requestDto.getUsername();
        String profile = requestDto.getProfile();
        String nickname = requestDto.getNickname();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원 중복 검증
        Optional<User> verificationUser = userRepository.findByUsername(username);
        if (verificationUser.isPresent()) {
            throw new IllegalArgumentException("중복된 사용자가 존재합니다.");
        }

        // 사용자 권한 확인
        UserRoleEnum role = UserRoleEnum.USER;
        if (requestDto.isAdmin()) {
            if (!ADMIN_TOKEN.equals(requestDto.getAdminToken())) {
                throw new IllegalArgumentException("관리자 인증키가 틀려 등록이 불가능합니다.");
            }
            role = UserRoleEnum.ADMIN;
        }

        // 사용자 등록
        User user = new User(username, password, nickname, profile, role);
        userRepository.save(user);
    }

    @Transactional
    public String followById(Long id, User user) {
        User userfollow = userRepository.findByUsername(user.getUsername()).orElseThrow(
                ()-> new IllegalArgumentException("해당 id의 사용자가 존재하지 않습니다.")
        );
        User target = userRepository.findById(id).orElseThrow(
                ()-> new IllegalArgumentException("해당 id의 사용자가 존재하지 않습니다.")
        );

        if(target.getStatus().equals("ON"))
            return userfollow.follow(id);
        else
            throw new IllegalArgumentException("해당 사용자는 팔로우 할 수 없는 상태입니다.");
    }

    public String deactivateUser(User user) {
        User target = userRepository.findByUsername(user.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다")
        );

        if(target.getStatus().equals("DEACTIVATED")){
            throw new IllegalArgumentException("해당 사용자는 이미 비활성화 상태입니다.");
        }

        if(target.getStatus().equals("DELETED")){
            throw new IllegalArgumentException("해당 사용자는 탈퇴된 상태입니다. 활성화된 사용자만 비활성화 할 수 있습니다.");
        }

        target.updateStatus("DEACTIVATED");
        return "비활성화 상태로 변경되었습니다.";
    }

    public String activateUser(User user) {
        User target = userRepository.findByUsername(user.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다")
        );

        if(target.getStatus().equals("ACTIVATED")){
            throw new IllegalArgumentException("해당 사용자는 이미 활성화 상태입니다.");
        }

        if(target.getStatus().equals("DELETED")){
            throw new IllegalArgumentException("해당 사용자는 탈퇴된 상태입니다. 탈퇴된 사용자는 관리자만 활성화 할 수 있습니다.");
        }

        target.updateStatus("ACTIVATED");
        return "활성화 상태로 변경되었습니다.";
    }

    public String deleteUser(User user) {
        User target = userRepository.findByUsername(user.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다")
        );

        if(target.getStatus().equals("DELETED")){
            throw new IllegalArgumentException("해당 사용자는 이미 탈퇴 상태입니다.");
        }

        target.updateStatus("DELETED");
        return "탈퇴 상태로 변경되었습니다";
    }
}
