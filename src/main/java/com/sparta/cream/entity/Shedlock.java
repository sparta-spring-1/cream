package com.sparta.cream.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Shedlock {
	@Id
	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "lock_until", nullable = false)
	private Instant lockUntil;

	@Column(name = "locked_at", nullable = false)
	private Instant lockedAt;

	@Column(name = "locked_by", nullable = false)
	private String lockedBy;
}
