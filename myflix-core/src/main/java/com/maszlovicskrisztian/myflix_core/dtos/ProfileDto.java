package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.Profile;

public record ProfileDto(Long id, String name, String avatarKey, String preferredLanguage) {
    public static ProfileDto from(Profile profile) {
        return new ProfileDto(profile.getId(), profile.getName(), profile.getAvatarKey(), profile.getPreferredLanguage());
    }
}
