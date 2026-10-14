package com.qbe.springstarter.repository;

import com.qbe.springstarter.entity.SampleFavoriteEntity;
import com.qbe.springstarter.id.SampleFavoriteId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleFavoriteRepository extends JpaRepository<SampleFavoriteEntity, SampleFavoriteId> {

    List<SampleFavoriteEntity> findByIdUsername(String username);
}
