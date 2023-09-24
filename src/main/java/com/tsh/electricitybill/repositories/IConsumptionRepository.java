package com.tsh.electricitybill.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tsh.electricitybill.entities.Consumption;
import java.util.List;
import java.time.LocalDate;

public interface IConsumptionRepository extends JpaRepository<Consumption, Long> {
  List<Consumption> findByMeterDateBetween(LocalDate firstDate, LocalDate secondDate);
}
