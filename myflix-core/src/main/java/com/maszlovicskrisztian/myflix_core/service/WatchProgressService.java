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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchProgressService {
    private final WatchProgressRepository watchProgressRepository;
    private final ProfileRepository profileRepository;
    private final FileInfoRepository fileInfoRepository;
    private final MediaBaseMapper mediaBaseMapper;
    private final TranslationService translationService;

    public Long getProgressSecondsForMediaByProfile(Long fileInfoId, Long profileId) {
        return watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, fileInfoId)
                .map(WatchProgress::getProgressSeconds)
                .orElse(0L);
    }

    public WatchProgressResponse getProgressForMediaByProfile(Long fileInfoId, Long profileId) {
        return watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, fileInfoId)
                .map(WatchProgressResponse::from)
                .orElse(null);
    }

    public WatchProgressResponse setProgressForMediaByProfile(Long fileInfoId, Long profileId, Long progressSeconds) {
        WatchProgress watchProgress = watchProgressRepository
                .findByProfileIdAndFileInfoId(profileId, fileInfoId)
                .orElseGet(() -> {
                    WatchProgress newProgress = new WatchProgress();
                    newProgress.setProfile(profileRepository.getReferenceById(profileId));
                    newProgress.setFileInfo(fileInfoRepository.getReferenceById(fileInfoId));
                    return newProgress;
                });

        watchProgress.setProgressSeconds(progressSeconds);
        watchProgress.setUpdatedAt(LocalDateTime.now());

        WatchProgress saved = watchProgressRepository.save(watchProgress);
        return WatchProgressResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<MediaBaseResponse> getMediasInWatchByProfile(Long profileId, String languageCode) {
        var medias = watchProgressRepository
                .findAllByProfileId(profileId)
                .stream().map(WatchProgress::getFileInfo)
                .filter(x -> x.getMovieMetadata() != null || x.getEpisodeMetadata() != null)
                .toList();

        List<MediaBaseResponse> result = new ArrayList<>();
        medias.forEach((x) -> {
            var title = translationService.translateMediaTitle(x, languageCode);
            result.add(mediaBaseMapper.fromContinueWatching(x, title));
        });

        return result;
    }
}
