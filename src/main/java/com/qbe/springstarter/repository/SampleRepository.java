package com.qbe.springstarter.repository;

import com.qbe.springstarter.entity.SampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<SampleEntity, Long> {}
