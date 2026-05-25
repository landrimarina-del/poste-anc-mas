package it.poste.anc.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter UserDetails che incapsula {@link AppUser} per Spring Security.
 * Espone anche i codici dei ruoli e dei gruppi (utili a /auth/me).
 */
public class AppUserDetails implements UserDetails {

    private final AppUser user;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(AppUser user) {
        this.user = user;
        // Mappa ogni ruolo come ROLE_<code> per consentire @PreAuthorize("hasRole('OPERATORE_ANC')").
        this.authorities = user.getRoles().stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.getCode()))
                .toList();
    }

    public AppUser getDomainUser() {
        return user;
    }

    public Set<String> getRoleCodes() {
        return user.getRoles().stream().map(Role::getCode).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public Set<String> getGroupCodes() {
        return user.getGroups().stream().map(UserGroup::getCode).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return user.isActive(); }
}
