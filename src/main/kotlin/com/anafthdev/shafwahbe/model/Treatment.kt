package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.PriceType
import com.anafthdev.shafwahbe.util.converters.DoubleListConverter
import com.anafthdev.shafwahbe.util.converters.StringListConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * @property prices Fixed price if contain 1 item, Range if 2 item, Option if > 2
 * @property active Indicate this treatment must displayed or not
 */
@Entity
@Table(name = "treatment")
data class Treatment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: TreatmentCategory = TreatmentCategory(),

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val active: Boolean = false,

    @Enumerated(EnumType.STRING)
    val priceType: PriceType = PriceType.Fixed,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = DoubleListConverter::class)
    val prices: List<Double> = listOf(),
)

