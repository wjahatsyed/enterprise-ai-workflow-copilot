package com.wajahat.aiworkflow.auth;

import com.wajahat.aiworkflow.common.BaseEntity;
import com.wajahat.aiworkflow.tenant.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "api_keys")
public class ApiKey extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_key_hash", nullable = false, unique = true)
    private String apiKeyHash;

    @Column(nullable = false)
    private boolean enabled = true;
}
