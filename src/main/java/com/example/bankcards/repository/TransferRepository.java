package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    @Query("""
            select t
            from Transfer t
            where t.fromCard.id = :cardId
               or t.toCard.id   = :cardId
            """)
    Page<Transfer> findByCardId(UUID cardId, Pageable pageable);

    @Query("""
            select t
            from Transfer t
            where t.fromCard.owner.id = :userId
               or t.toCard.owner.id   = :userId
            """)
    Page<Transfer> findByUserId(UUID userId, Pageable pageable);
}
