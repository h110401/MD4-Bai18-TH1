package rikkei.academy.service;

import rikkei.academy.model.Role;
import rikkei.academy.model.RoleName;

import java.util.Optional;

public interface IRoleService {
    Optional<Role> findByName(RoleName name);
}
