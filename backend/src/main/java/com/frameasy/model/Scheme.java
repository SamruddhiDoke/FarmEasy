package com.frameasy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "schemes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @Column(length = 500)
    private String timeline;

    @Column(length = 100)
    private String state;

    @Column(name = "official_url")
    private String officialUrl;

    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "fetched_at")
    private Instant fetchedAt = Instant.now();
}
