package com.frameasy.repository;

import com.frameasy.model.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    List<Scheme> findByState(String state);
    List<Scheme> findByStateContainingIgnoreCase(String state);
}
