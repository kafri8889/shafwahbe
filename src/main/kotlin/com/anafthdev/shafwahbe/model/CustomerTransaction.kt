package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.PaymentMethod
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entitas untuk mencatat setiap transaksi treatment atau paket yang diambil pelanggan.
 *
 * @property customer Data customer
 * @property employee Employee yg menghandle
 * @property actualPrice Harga aktual yang dibayarkan untuk treatment/paket ini pada saat transaksi (bisa berbeda dari harga master jika ada diskon/penyesuaian).
 * @property notes Catatan tambahan spesifik untuk transaksi ini (misalnya, permintaan khusus pelanggan, kondisi saat treatment).
 * @property paymentMethod Metode pembayaran yang digunakan.
 */
@Entity
@Table(name = "customer_transaction")
data class CustomerTransaction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer = Customer(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    val employee: Employee = Employee(),

    @Column(nullable = false)
    val actualPrice: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @Column(columnDefinition = "TEXT")
    val notes: String = "",

    @Column(nullable = false)
    val date: LocalDateTime = LocalDateTime.now(),

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "transaction_id")
    val items: MutableList<CustomerTransactionItem> = mutableListOf()
)

