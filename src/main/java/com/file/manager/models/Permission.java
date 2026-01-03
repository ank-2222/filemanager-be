package com.file.manager.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "permission", schema = "filesystem")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Permission {
    @Id
    @Column(nullable = false)
    private UUID id;

    @JoinColumn(nullable = false)
    private UUID fileId;

    @Column(nullable = false)
    private UUID ownerId;  // FK to accounts.user.id

    @Column(nullable = false)
    private Boolean isPublic = false;

    @Column
    private UUID shareWithId;

    @Column(nullable = false)
    private Boolean canView = false;

    @Column(nullable = false)
    private Boolean canEdit = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}