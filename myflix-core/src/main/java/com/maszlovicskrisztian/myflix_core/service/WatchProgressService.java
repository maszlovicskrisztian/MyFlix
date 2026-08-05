package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.dtos.WatchProgressDto;
import com.maszlovicskrisztian.myflix_core.mapping.MediaItemMapper;
import com.maszlovicskrisztian.myflix_core.model.WatchProgress;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.ProfileRepository;
import com.maszlovicskrisztian.myflix_core.repository.WatchProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchProgressService {
    private final WatchProgressRepository watchProgressRepository;
    private final ProfileRepository profileRepository;
    private final FileInfoRepository fileInfoRepository;
    private final MediaItemMapper mapper;

    public WatchProgressDto getProgressForMediaByProfile(Long id, Long profileId) {
        return watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, id)
                .map(WatchProgressDto::from)
                .orElse(null);
    }

    public WatchProgressDto setProgressForMediaByProfile(Long id, Long profileId, Long progressSeconds) {
        WatchProgress watchProgress = watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, id)
                .orElseGet(() -> {
                    WatchProgress newProgress = new WatchProgress();
                    newProgress.setProfile(profileRepository.getReferenceById(profileId));
                    newProgress.setFileInfo(fileInfoRepository.getReferenceById(id));
                    return newProgress;
                });

        watchProgress.setProgressSeconds(progressSeconds);
        watchProgress.setUpdatedAt(LocalDateTime.now());

        WatchProgress saved = watchProgressRepository.save(watchProgress);
        return WatchProgressDto.from(saved);
    }

    public List<MediaItemDto> getMediasInWatchByProfile(Long profileId) {
        List<WatchProgress> progressList = watchProgressRepository.findAllByProfileId(profileId).orElse(null);

        if (progressList == null)
            return null;

        return progressList.stream().map(WatchProgress::getFileInfo).map(mapper::from).toList();
    }
}
