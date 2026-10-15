package com.qbe.springstarter.repository;

import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SampleRepository extends JpaRepository<SampleEntity, Long>, JpaSpecificationExecutor<SampleEntity> {

    long countByActiveTrue();

    long countByActiveFalse();

    @Query(
            """
       select count(s)
       from SampleEntity s
       where s.stock is null
          or s.stock <= 0
       """)
    long countWithoutStock();

    long countByStockGreaterThan(Long value);

    long countByStatus(Status status);

    @Query("""
       select s.status, count(s)
       from SampleEntity s
       group by s.status
       """)
    List<Object[]> countByStatus();

    @Query("""
       select s.category, count(s)
       from SampleEntity s
       group by s.category
       """)
    List<Object[]> countByCategory();

    @Query(
            value =
                    """
       select to_char(created_at, 'YYYY-MM'),
              count(*)
       from mydb.sample_entity
       where created_at is not null
       group by to_char(created_at, 'YYYY-MM')
       order by to_char(created_at, 'YYYY-MM')
       """,
            nativeQuery = true)
    List<Object[]> countByCreationMonth();

    @Query(
            """
       select min(s.price),
              max(s.price),
              avg(s.price)
       from SampleEntity s
       """)
    List<Object[]> priceStatistics();

    @Query(
            """
       select min(s.quantity),
              max(s.quantity),
              avg(s.quantity)
       from SampleEntity s
       """)
    List<Object[]> quantityStatistics();
}
