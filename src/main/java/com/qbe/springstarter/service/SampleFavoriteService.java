package com.qbe.springstarter.service;

import com.qbe.springstarter.constants.SpringStarterConstants;
import com.qbe.springstarter.entity.SampleFavoriteEntity;
import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.id.SampleFavoriteId;
import com.qbe.springstarter.repository.SampleFavoriteRepository;
import com.qbe.springstarter.repository.SampleRepository;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SampleFavoriteService implements ISampleFavoriteService {

    private final SampleRepository sampleRepository;
    private final SampleFavoriteRepository sampleFavoriteRepository;

    @Override
    @Transactional
    @CacheEvict(value = "samples", key = "#sampleId + '_' + @securityUtils.getCurrentUsername()")
    public void addFavorite(Long sampleId) {
        if (!sampleRepository.existsById(sampleId)) {
            throw new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, sampleId);
        }

        String username = getCurrentUsername();

        log.info("ADD favorite sampleId={} username={}", sampleId, username);

        SampleFavoriteId favoriteId = new SampleFavoriteId(sampleId, username);

        if (sampleFavoriteRepository.existsById(favoriteId)) {
            return;
        }

        SampleFavoriteEntity favorite = new SampleFavoriteEntity();
        favorite.setId(favoriteId);
        favorite.setCreatedAt(Instant.now());

        sampleFavoriteRepository.save(favorite);
        log.info("Sample added to favorites sampleId={} username={}", sampleId, username);
    }

    @Override
    @Transactional
    @CacheEvict(value = "samples", key = "#sampleId + '_' + @securityUtils.getCurrentUsername()")
    public void removeFavorite(Long sampleId) {
        if (!sampleRepository.existsById(sampleId)) {
            throw new NotFoundException(SpringStarterConstants.SAMPLE_ENTITY_RESOURCE_NAME, sampleId);
        }
        String username = getCurrentUsername();
        SampleFavoriteId favoriteId = new SampleFavoriteId(sampleId, username);
        sampleFavoriteRepository.deleteById(favoriteId);
        log.info("Sample removed from favorites sampleId={} username={}", sampleId, username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long sampleId) {
        String username = getCurrentUsername();
        boolean favorite = sampleFavoriteRepository.existsById(new SampleFavoriteId(sampleId, username));
        log.debug("Favorite status for sampleId={} username={} : {}", sampleId, username, favorite);
        return favorite;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getUserFavoriteSampleIds() {
        String username = getCurrentUsername();
        return sampleFavoriteRepository.findByIdUsername(username).stream()
                .map(favorite -> favorite.getId().getSampleId())
                .collect(Collectors.toSet());
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return authentication.getName();
    }
}
