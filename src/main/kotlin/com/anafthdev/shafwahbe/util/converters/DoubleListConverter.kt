package com.anafthdev.shafwahbe.util.converters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class DoubleListConverter : AttributeConverter<List<Double>, String> {

    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Double>?): String {
        return objectMapper.writeValueAsString(attribute ?: emptyList<Double>())
    }

    override fun convertToEntityAttribute(dbData: String?): List<Double> {
        return if (dbData.isNullOrBlank()) emptyList()
        else objectMapper.readValue(dbData, object : TypeReference<List<Double>>() {})
    }
}