package com.projectsaas.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String resource; // project, task, user, etc.

    @Column(length = 50)
    private String action;   // create, read, update, delete

    // Relations Many-to-Many avec Role
    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles;

    // Méthode utilitaire pour créer le nom de permission
    public static String createPermissionName(String resource, String action) {
        return resource.toUpperCase() + "_" + action.toUpperCase();
    }
}