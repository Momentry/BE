package com.momentry.BE.domain.user.service;


import com.momentry.BE.domain.user.entity.AlertPreference;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.exception.AlertPreferenceNotFoundException;
import com.momentry.BE.domain.user.repository.AlertPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertPreferenceService {
    private final AlertPreferenceRepository alertPreferenceRepository;

    public void saveAlertPreference(AlertPreference alertPreference){
        alertPreferenceRepository.save(alertPreference);
    }

    public AlertPreference getOrCreateAlertPreference(User user){
        return alertPreferenceRepository.findById(user.getId()).orElseGet(()->{
            AlertPreference alertPreference = new AlertPreference(user,false, false, false);
            saveAlertPreference(alertPreference);
            return alertPreference;
        });
    }

    public AlertPreference getAlertPreference(User user){
        return alertPreferenceRepository.findById(user.getId()).orElseThrow(AlertPreferenceNotFoundException::new);
    }
}
