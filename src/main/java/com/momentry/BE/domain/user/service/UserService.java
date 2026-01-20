package com.momentry.BE.domain.user.service;

import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.exception.DuplicateUserException;
import com.momentry.BE.domain.user.exception.UserNotFoundException;
import com.momentry.BE.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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
}
