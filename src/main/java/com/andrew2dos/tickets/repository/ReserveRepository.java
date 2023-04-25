package com.andrew2dos.tickets.repository;

import com.andrew2dos.tickets.dto.ReserveDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReserveRepository extends JpaRepository<ReserveDto, Long> {
}
