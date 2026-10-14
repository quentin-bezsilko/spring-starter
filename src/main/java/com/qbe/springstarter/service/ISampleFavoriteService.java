package com.qbe.springstarter.service;

import java.util.Set;

public interface ISampleFavoriteService {

    void addFavorite(Long sampleId);

    void removeFavorite(Long sampleId);

    boolean isFavorite(Long sampleId);

    Set<Long> getUserFavoriteSampleIds();
}
