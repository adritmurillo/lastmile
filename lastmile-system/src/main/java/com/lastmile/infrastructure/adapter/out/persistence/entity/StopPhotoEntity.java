package com.lastmile.infrastructure.adapter.out.persistence.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stop_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopPhotoEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private StopEntity stop;

    @Column(name = "photo_url",nullable = false)
    private String photoUrl;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(name = "photo_order", nullable = false)
    private int photoOrder;

}
