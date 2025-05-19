package com.effective.linkshorter.repository;

import com.effective.linkshorter.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUniqueIdentifierAndSessionId(String uniqueIdentifier);
}
