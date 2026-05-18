package it.poste.anc.shared.security;

import jakarta.persistence.*;

/** Ruolo applicativo (RBAC): OPERATORE_ANC, SUPERVISORE_ANC, ADMIN. */
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 64)
    private String name;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
