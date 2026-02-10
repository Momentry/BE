package com.momentry.BE.domain.user.service.sub;

import com.momentry.BE.domain.user.dto.UpdateUserInfoRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.dto.UserUpdateResponse;
import com.momentry.BE.domain.user.entity.AccountPlan;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.enums.UserAccountPlan;
import com.momentry.BE.domain.user.exception.AccountPlanNotFoundException;
import com.momentry.BE.domain.user.exception.DuplicateUserException;
import com.momentry.BE.domain.user.exception.MismatchUserException;
import com.momentry.BE.domain.user.exception.UserNotFoundException;
import com.momentry.BE.domain.user.repository.AccountPlanRepository;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountPlanRepository accountPlanRepository;

    // base method - read
    public User getUser(Long userId){
        return userRepository.findActiveUserById(userId).orElseThrow(UserNotFoundException::new);
    }

    public User getCurrentUser(Long userId){
        if(!userId.equals(SecurityUtil.getCurrentUserId())){
            throw new MismatchUserException();
        }

        return getUser(userId);
    }

    // base method - create & update
    // 회원 정보 수정, 회원 가입에 공통으로 사용
    public void saveUser(User user){
        try{
            userRepository.save(user);
        } catch(DataIntegrityViolationException e){
            throw new DuplicateUserException();
        }
    }

    // 회원 탈퇴 - soft delete
    public void withdrawUser(User user) {
        user.setIsActive(false);
        saveUser(user);
    }

    // 회원 복구
    public void restoreUser(User user) {
        user.setIsActive(true);
        saveUser(user);
    }

    // 정보 수정
    public UserUpdateResponse update(Long userId, UpdateUserInfoRequest request){
        User currentUser = getCurrentUser(userId);

        // 닉네임이 있으면 업데이트
        if (request.getNewUsername() != null && !request.getNewUsername().trim().isEmpty()) {
            currentUser.setUsername(request.getNewUsername());
        }

        // 업데이트 반영
        saveUser(currentUser);

        return new UserUpdateResponse(currentUser.getUsername());
    }

    // 사용자 조회 -> 없으면 회원 가입 -> 반환
    public User findOrCreateUser(OidcClaims claims, String provider) {
        String sub = claims.getSub();
        
        return userRepository.findByProviderAndProviderId(provider, sub)
                .orElseGet(() -> joinUser(claims, provider));
    }

    // 회원 가입
    public User joinUser(OidcClaims claims, String provider){
        String sub = claims.getSub();
        String email = claims.getEmail();
        String name = claims.getName();
        String picture = claims.getPicture();

        AccountPlan accountPlan = accountPlanRepository
                                    .findByPlan(UserAccountPlan.FREE_USER.getPlan())
                                    .orElseThrow(AccountPlanNotFoundException::new);


        User newUser = new User(email, name, provider, sub, picture, accountPlan);

        saveUser(newUser);

        return newUser;
    }


}
