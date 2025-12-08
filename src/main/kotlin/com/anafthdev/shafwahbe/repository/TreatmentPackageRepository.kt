package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.TreatmentPackage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TreatmentPackageRepository : JpaRepository<TreatmentPackage, Long>
