package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.WatchProgressResponse;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
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
    private final MediaBaseMapper mediaBaseMapper;

    public Long getProgressSecondsForMediaByProfile(Long id, Long profileId) {
        return watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, id)
                .map(WatchProgress::getProgressSeconds)
                .orElse(0L);
    }

    public WatchProgressResponse getProgressForMediaByProfile(Long id, Long profileId) {
        return watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, id)
                .map(WatchProgressResponse::from)
                .orElse(null);
    }

    public WatchProgressResponse setProgressForMediaByProfile(Long id, Long profileId, Long progressSeconds) {
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
        return WatchProgressResponse.from(saved);
    }

    public List<MediaBaseResponse> getMediasInWatchByProfile(Long profileId) {
        List<WatchProgress> progressList = watchProgressRepository.findAllByProfileId(profileId).orElse(null);

        if (progressList == null)
            return null;

        return progressList.stream().map(WatchProgress::getFileInfo).map(mediaBaseMapper::fromContinueWatching).toList();
    }
}
