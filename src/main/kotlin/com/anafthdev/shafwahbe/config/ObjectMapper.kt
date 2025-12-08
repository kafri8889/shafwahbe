package com.anafthdev.shafwahbe.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import org.springframework.context.annotation.Bean
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//@Bean
//fun objectMapper(): ObjectMapper {
//    val mapper = ObjectMapper()
//    mapper.registerModule(JavaTimeModule())
//    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//    mapper.registerModule(JavaTimeModule().apply {
//        addDeserializer(
//            LocalDateTime::class.java,
//            LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//        )
//    })
//    return mapper
//}
