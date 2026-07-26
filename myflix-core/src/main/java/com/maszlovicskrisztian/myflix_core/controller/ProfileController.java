package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.ProfileDto;
import com.maszlovicskrisztian.myflix_core.model.Profile;
import com.maszlovicskrisztian.myflix_core.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileRepository profileRepository;

    @GetMapping
    public List<ProfileDto> getAllProfiles() {
        return profileRepository.findAll().stream().map(ProfileDto::from).toList();
    }

    @PostMapping
    public ProfileDto saveProfile(@RequestBody ProfileDto profileDto) {
        if (profileDto == null)
            return null;

        Profile profile = new Profile();
        profile.setName(profileDto.name());
        profile.setAvatarKey(profileDto.avatarKey());

        profileRepository.save(profile);
        return ProfileDto.from(profile);
    }

    @DeleteMapping("/{id}")
    public void deleteProfile(@PathVariable Long id) {
        profileRepository.deleteById(id);
    }
}
