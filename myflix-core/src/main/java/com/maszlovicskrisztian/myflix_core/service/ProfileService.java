package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.ProfileDto;
import com.maszlovicskrisztian.myflix_core.model.Profile;
import com.maszlovicskrisztian.myflix_core.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;

    public Profile saveProfile(ProfileDto profileDto) {
        Profile profile = new Profile();
        profile.setName(profileDto.name());
        profile.setAvatarKey(profileDto.avatarKey());

        return profileRepository.save(profile);
    }

    public void deleteById(Long id) {
        profileRepository.deleteById(id);
    }
}
