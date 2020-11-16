package com.ruffin.SPRING_BATCH_V2.config;

import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Convertisseur pour prendre en charge de nouvelles API de temps avec H2.
 * Fournit une conversion entre Instant et Timestamp.
 */
@Converter(autoApply = true)
public class InstantTimeJPAConverter implements AttributeConverter<Instant, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(Instant instant) {
		return (instant == null ? null : Timestamp.from(instant));
	}

	@Override
	public Instant convertToEntityAttribute(Timestamp timestamp) {
		return (timestamp == null ? null : timestamp.toInstant());
	}

}
