package com.momentry.BE.domain.user.service;


import com.momentry.BE.domain.user.entity.AlertPreference;
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
}
