package com.tsh.electricitybill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyConsumptionDTO {
  private String date;
  private Double energy;
}
