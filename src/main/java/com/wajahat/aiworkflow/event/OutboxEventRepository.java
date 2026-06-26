package com.wajahat.aiworkflow.event;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
            select event
            from OutboxEvent event
            where event.status in :statuses
            and (event.nextRetryAt is null or event.nextRetryAt <= :now)
            order by event.createdAt asc
            """)
    List<OutboxEvent> findReadyEvents(
            @Param("statuses") Collection<OutboxEventStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    long countByStatus(OutboxEventStatus status);
}
