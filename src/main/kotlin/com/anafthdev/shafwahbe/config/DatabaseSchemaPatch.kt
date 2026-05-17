package com.anafthdev.shafwahbe.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseSchemaPatch(
    private val jdbcTemplate: JdbcTemplate
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        jdbcTemplate.execute(
            """
            INSERT INTO staff (id, name, role, phone_number, active)
            SELECT e.id, e.name, COALESCE(CAST(e.role AS VARCHAR), 'Stylist'), e.phone_number, true
            FROM employee e
            WHERE NOT EXISTS (
                SELECT 1 FROM staff s WHERE s.id = e.id
            )
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            SELECT setval(
                pg_get_serial_sequence('staff', 'id'),
                COALESCE((SELECT MAX(id) FROM staff), 1),
                true
            )
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE customer_transaction
            DROP CONSTRAINT IF EXISTS fksg2abo962su6aujjmcflp81fc
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE customer_transaction
            DROP CONSTRAINT IF EXISTS fk_customer_transaction_staff
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE customer_transaction
            ADD CONSTRAINT fk_customer_transaction_staff
            FOREIGN KEY (employee_id) REFERENCES staff(id)
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE customer_transaction_item
            ADD COLUMN IF NOT EXISTS employee_id BIGINT
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            UPDATE customer_transaction_item cti
            SET employee_id = ct.employee_id
            FROM customer_transaction ct
            WHERE cti.transaction_id = ct.id
              AND cti.employee_id IS NULL
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE customer_transaction
            ADD COLUMN IF NOT EXISTS voucher_discount_amount DOUBLE PRECISION NOT NULL DEFAULT 0
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE recurring_expense
            DROP CONSTRAINT IF EXISTS recurring_expense_frequency_check
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE recurring_expense
            ADD CONSTRAINT recurring_expense_frequency_check
            CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'))
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE treatment
            ADD COLUMN IF NOT EXISTS staff_commission_type VARCHAR(50) NOT NULL DEFAULT 'PERCENTAGE'
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE treatment
            ALTER COLUMN staff_commission_type TYPE VARCHAR(50)
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE treatment
            ALTER COLUMN staff_commission_type SET DEFAULT 'PERCENTAGE'
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE treatment
            ADD COLUMN IF NOT EXISTS staff_commission_value DOUBLE PRECISION NOT NULL DEFAULT 0
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE treatment
            ALTER COLUMN staff_commission_value SET DEFAULT 0
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            UPDATE treatment
            SET staff_commission_type = 'PERCENTAGE'
            WHERE staff_commission_type IS NULL
               OR staff_commission_type = ''
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            UPDATE treatment
            SET staff_commission_type = 'PERCENTAGE',
                staff_commission_value = 10
            WHERE LOWER(title) IN (
                'gunting rambut',
                'gunting blow dry',
                'gunting cuci blow dry',
                'poni',
                'cuci blow dry',
                'cuci',
                'blow',
                'curly / blow tarik',
                'catok + vitamin',
                'creambath anak (2-7 tahun)',
                'creambath a plus pijat bahu dada',
                'creambath b',
                'hair spa a, plus pijat bahu dada',
                'hair spa b',
                'hair mask natural non pijat',
                'hair mask keratin/matrix non pijat',
                'cat rambut',
                'bleaching rambut',
                'smoothing (offer tebal, panjang + 100)',
                'therapy oxygen rambut',
                'pijat tangan',
                'ratus v',
                'kerik badan',
                'pedicure + spa kaki + vitamin kuku',
                'manicure + massage jari + vitamin kuku',
                'paket manicure + pedicure + vit',
                'body steam/sauna (+ rempah 10k)',
                'totok aura wajah',
                'paket masker + totok aura wajah'
            )
              AND staff_commission_value = 0
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            UPDATE treatment
            SET staff_commission_type = 'PERCENTAGE',
                staff_commission_value = 15
            WHERE LOWER(title) IN (
                'lulur (body scrub)',
                'masker badan',
                'full body massage',
                'bleaching badan'
            )
              AND staff_commission_value = 0
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            UPDATE treatment
            SET staff_commission_type = 'PERCENTAGE',
                staff_commission_value = 5
            WHERE LOWER(title) IN (
                'only mask acne',
                'facial acne, tea tree oil',
                'facial oxy acne',
                'facial laser ipl acne',
                'facial whitening',
                'facial detox glow',
                'facial oxy glow',
                'facial microdermabrasi',
                'facial ipl rejuve',
                'facial rf/lifting',
                'facial dermapen + soft peel',
                'facial cc glow'
            )
              AND staff_commission_value = 0
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            ALTER TABLE IF EXISTS member_voucher
            ALTER COLUMN snapshot_discount_type SET DEFAULT 'FIXED'
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            UPDATE member_voucher mv
            SET
                snapshot_name = vt.name,
                snapshot_description = vt.description,
                snapshot_discount_type = vt.discount_type,
                snapshot_discount_value = vt.discount_value,
                snapshot_minimum_transaction = vt.minimum_transaction,
                snapshot_validity_days = vt.validity_days,
                snapshot_applies_to_all = vt.applies_to_all
            FROM voucher_template vt
            WHERE mv.template_id = vt.id
              AND (mv.snapshot_name IS NULL OR mv.snapshot_name = '')
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            INSERT INTO member_voucher_treatment_ids (member_voucher_id, treatment_id)
            SELECT mv.id, vtt.treatment_id
            FROM member_voucher mv
            JOIN voucher_template_treatment_ids vtt ON vtt.voucher_template_id = mv.template_id
            WHERE NOT EXISTS (
                SELECT 1
                FROM member_voucher_treatment_ids mvt
                WHERE mvt.member_voucher_id = mv.id
                  AND mvt.treatment_id = vtt.treatment_id
            )
            """.trimIndent()
        )

        jdbcTemplate.execute(
            """
            INSERT INTO member_voucher_package_ids (member_voucher_id, treatment_package_id)
            SELECT mv.id, vtp.treatment_package_id
            FROM member_voucher mv
            JOIN voucher_template_package_ids vtp ON vtp.voucher_template_id = mv.template_id
            WHERE NOT EXISTS (
                SELECT 1
                FROM member_voucher_package_ids mvp
                WHERE mvp.member_voucher_id = mv.id
                  AND mvp.treatment_package_id = vtp.treatment_package_id
            )
            """.trimIndent()
        )
    }
}
