package com.qbe.springstarter.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SampleFavoriteId implements Serializable {

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "username")
    private String username;
}
