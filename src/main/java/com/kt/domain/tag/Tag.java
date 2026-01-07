package com.kt.domain.tag;

import com.kt.common.Preconditions;
import com.kt.common.api.ErrorCode;
import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.pet.PetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

@Getter
@Entity
@Table(name = "tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseSoftDeleteEntity {

    @Column(name = "tag_key", nullable = false, length = 80)
    private String key;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetType petType;

    @Column(nullable = false)
    private boolean active;

    private Tag(String key, String name, PetType petType) {
        this.key = validateKey(key);
        this.name = validateName(name);
        this.petType = petType;
        this.active = true;
    }

    public static Tag create (String key, String name, PetType petType) {
        return new Tag(key, name, petType);
    }

    public void update(String key, String name, PetType petType) {
        assertNotDeleted();
        this.key = validateKey(key);
        this.name = validateName(name);
        this.petType = petType;
    }

    public void deactivate() {
        assertNotDeleted();
        this.active = false;
    }

    public void activate() {
        assertNotDeleted();
        this.active = true;
    }

    public void softDelete(Long deleterId) {
        assertNotDeleted();
        this.active = false;
        markDeleted(deleterId);
    }

    private String validateKey(String key) {
        Preconditions.validate(Strings.isNotBlank(key), ErrorCode.TAG_KEY_REQUIRED);
        Preconditions.validate(key.length() <= 80, ErrorCode.TAG_KEY_TOO_LONG);
        Preconditions.validate(key.matches("^[A-Z0-9_]+$"), ErrorCode.TAG_KEY_INVALID_FORMAT);
        Preconditions.validate(key.contains("_"), ErrorCode.TAG_KEY_INVALID_FORMAT);
        Preconditions.validate(!key.contains("__"), ErrorCode.TAG_KEY_INVALID_FORMAT);
        return key;
    }

    private String validateName(String value) {
        Preconditions.validate(Strings.isNotBlank(value), ErrorCode.TAG_NAME_REQUIRED);
        Preconditions.validate(value.length() <= 100, ErrorCode.TAG_NAME_TOO_LONG);
        return value;
    }

    private void assertNotDeleted() {
        Preconditions.validate(!deleted, ErrorCode.TAG_ALREADY_DELETED);
    }
}
