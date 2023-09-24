package com.tsh.electricitybill.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consumption")
@Data
@NoArgsConstructor
public class Consumption {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "active_energy")
  private Double activeEnergy;

  @Column(name = "meter_date")
  private LocalDate meterDate;

  @Column(name = "meter_time")
  private LocalTime meterTime;
}
