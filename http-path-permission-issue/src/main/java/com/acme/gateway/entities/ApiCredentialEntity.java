package com.acme.gateway.entities;

import com.acme.gateway.entities.converters.EncryptionConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "api_credential")
public class ApiCredentialEntity extends PanacheEntityBase {

    @EmbeddedId
    public CompositeApiId id;

    @MapsId("subscriptionId")
    @ManyToOne(fetch = LAZY)
    public SubscriptionEntity subscription;

    @Convert(converter = EncryptionConverter.class)
    public String username;

    @Convert(converter = EncryptionConverter.class)
    public String password;

    @Convert(converter = EncryptionConverter.class)
    public String clientId;

    @Convert(converter = EncryptionConverter.class)
    public String clientSecret;

    @Convert(converter = EncryptionConverter.class)
    public String apiKey;
}
