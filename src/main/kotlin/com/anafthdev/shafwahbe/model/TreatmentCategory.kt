package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.util.converters.StringListConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "treatment_category")
data class TreatmentCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val subTitle: String = "",

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    val notes: List<String> = listOf()
)

