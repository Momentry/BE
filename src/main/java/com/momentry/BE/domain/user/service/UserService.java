package com.momentry.BE.domain.user.service;

import com.momentry.BE.domain.user.dto.OidcClaims;
import com.momentry.BE.domain.user.entity.AccountPlan;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.enums.UserAccountPlan;
import com.momentry.BE.domain.user.exception.AccountPlanNotFoundException;
import com.momentry.BE.domain.user.exception.DuplicateUserException;
import com.momentry.BE.domain.user.exception.UserNotFoundException;
import com.momentry.BE.domain.user.repository.AccountPlanRepository;
import com.momentry.BE.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountPlanRepository accountPlanRepository;

    // base method - read
    public User getUser(Long userId){
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
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

    // 사용자 조회 -> 없으면 회원 가입 -> 반환
    public User findOrCreateUser(OidcClaims claims, String provider) {
        String sub = claims.getSub();
        
        return userRepository.findByProviderAndProviderId(provider, sub)
                .orElseGet(() -> joinUser(claims, provider));
    }

    // 회원 가입
    private User joinUser(OidcClaims claims, String provider){
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
