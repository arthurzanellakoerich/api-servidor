package com.api.arthur.repositorys;

import com.api.arthur.entitys.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
