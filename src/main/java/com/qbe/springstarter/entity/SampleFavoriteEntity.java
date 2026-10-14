package com.qbe.springstarter.entity;

import com.qbe.springstarter.id.SampleFavoriteId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sample_favorite", schema = "mydb")
public class SampleFavoriteEntity {

    @EmbeddedId
    private SampleFavoriteId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
