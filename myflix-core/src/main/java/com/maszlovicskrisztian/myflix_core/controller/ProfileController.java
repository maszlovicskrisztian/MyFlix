package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.ProfileDto;
import com.maszlovicskrisztian.myflix_core.model.Profile;
import com.maszlovicskrisztian.myflix_core.repository.ProfileRepository;
import com.maszlovicskrisztian.myflix_core.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    @GetMapping
    public List<ProfileDto> getAllProfiles() {
        return profileRepository.findAll().stream().map(ProfileDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<ProfileDto> saveProfile(@RequestBody ProfileDto profileDto) {
        Profile profile = profileService.saveProfile(profileDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfileDto.from(profile));
    }

    @DeleteMapping("/{id}")
    public void deleteProfile(@PathVariable Long id) {
        profileService.deleteById(id);
    }
}
