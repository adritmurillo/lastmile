package com.lastmile.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "bulk_load_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkLoadErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulk_load_id", nullable = false)
    private BulkLoadEntity bulkLoad;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "error_description", nullable = false)
    private String errorDescription;

    @Column(name = "problematic_value")
    private String problematicValue;
}