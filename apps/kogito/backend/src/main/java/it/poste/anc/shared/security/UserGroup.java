package it.poste.anc.shared.security;

import jakarta.persistence.*;

/** Gruppo utenti (es. GRUPPO_OPERATORE_ANC) — candidate group per task BC2. */
@Entity
@Table(name = "user_group")
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
