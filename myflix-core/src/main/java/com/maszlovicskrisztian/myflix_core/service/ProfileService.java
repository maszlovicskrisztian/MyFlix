package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.ProfileDto;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.model.Profile;
import com.maszlovicskrisztian.myflix_core.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;

    public List<ProfileDto> getAllProfiles() {
        return profileRepository.findAll().stream().map(ProfileDto::from).toList();
    }

    public Profile saveProfile(ProfileDto profileDto) {
        Profile profile = new Profile();
        profile.setName(profileDto.name());
        profile.setAvatarKey(profileDto.avatarKey());
        profile.setPreferredLanguage(profileDto.preferredLanguage());

        Profile saved = profileRepository.save(profile);
        log.info("Saved new profile: {}", profileDto.name());
        return saved;
    }

    public void deleteById(Long id) {
        if (!profileRepository.existsById(id))
            throw new ResourceNotFoundException("Profile does not exists with id: " + id);

        profileRepository.deleteById(id);
    }
}
