package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.repository.SampleRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TestSampleRepositoryIT extends TestAbstractIntegration {

    @Autowired
    private SampleRepository sampleRepository;

    @Test
    void shouldSaveEntity() {
        SampleEntity entity = buildSampleEntity("REPOSITORY_TEST_PRODUCT-1-" + UUID.randomUUID());
        SampleEntity saved = sampleRepository.save(entity);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(entity.getName());
    }

    @Test
    void shouldFindEntityById() {
        SampleEntity entity = buildSampleEntity("REPOSITORY_TEST_PRODUCT-3-" + UUID.randomUUID());
        SampleEntity saved = sampleRepository.save(entity);

        Optional<SampleEntity> result = sampleRepository.findById(saved.getId());

        assertThat(result).isPresent().hasValueSatisfying(found -> {
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getName()).isEqualTo(entity.getName());
        });
    }

    @Test
    void shouldFindAllEntities() {
        SampleEntity entity1 = buildSampleEntity("REPOSITORY_TEST_PRODUCT-4-" + UUID.randomUUID());
        SampleEntity entity2 = buildSampleEntity("REPOSITORY_TEST_PRODUCT-5-" + UUID.randomUUID());

        sampleRepository.save(entity1);
        sampleRepository.save(entity2);

        var result = sampleRepository.findAll();

        assertThat(result)
                .isNotEmpty()
                .extracting(SampleEntity::getName)
                .contains(entity1.getName(), entity2.getName());
    }

    @Test
    void shouldDeleteEntity() {
        SampleEntity entity = buildSampleEntity("REPOSITORY_TEST_PRODUCT-TO_DELETE-" + UUID.randomUUID());
        SampleEntity saved = sampleRepository.save(entity);

        sampleRepository.deleteById(saved.getId());

        Optional<SampleEntity> result = sampleRepository.findById(saved.getId());
        assertThat(result).isEmpty();
        assertThat(sampleRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void shouldCheckEntityExists() {
        SampleEntity entity = buildSampleEntity("REPOSITORY_TEST_PRODUCT-EXISTS_TEST-" + UUID.randomUUID());
        SampleEntity saved = sampleRepository.save(entity);

        boolean exists = sampleRepository.existsById(saved.getId());
        assertThat(exists).isTrue();
    }
}
