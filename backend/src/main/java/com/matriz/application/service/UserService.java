package com.matriz.application.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import com.matriz.application.port.in.UserUseCases;
import com.matriz.application.port.out.persistence.DomainUserRepository;
import com.matriz.domain.user.ExternalUser;
import com.matriz.domain.user.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UserService implements UserUseCases {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());
    private static final int DIRECTORY_LIMIT = 20;

    private final DomainUserRepository userRepository;

    @Override
    @Transactional
    public User registerLogin(UUID id, String name, String email) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            User user = new User(id, name, email, true);
            userRepository.save(user);
            return user;
        }
        User user = existing.get();
        if (user.refresh(name, email, user.isActive())) {
            userRepository.save(user);
        }
        return user;
    }

    @Override
    @Transactional
    public void synchronise(Collection<ExternalUser> externalUsers) {
        Set<UUID> seen = new HashSet<>();
        for (ExternalUser external : externalUsers) {
            seen.add(external.id());
            Optional<User> existing = userRepository.findById(external.id());
            if (existing.isEmpty()) {
                if (userRepository.emailUsedByAnother(external.email(), external.id())) {
                    LOG.warning(() -> "Skipping external user " + external.id()
                            + ": email already used by another user");
                    continue;
                }
                userRepository.save(new User(external.id(), external.name(), external.email(), external.active()));
            } else if (existing.get().refresh(external.name(), external.email(), external.active())) {
                userRepository.save(existing.get());
            }
        }
        if (!seen.isEmpty()) {
            userRepository.deactivateAllExcept(seen);
        }
    }

    @Override
    public List<User> searchActive(String text) {
        return userRepository.searchActive(text == null ? "" : text.strip(), DIRECTORY_LIMIT);
    }
}
