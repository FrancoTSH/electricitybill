package com.tsh.electricitybill.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

import com.tsh.electricitybill.dto.DailyConsumptionDTO;
import com.tsh.electricitybill.dto.HourlyConsumptionDTO;

import java.util.List;
import java.time.LocalDate;

public interface IConsumptionService {
  ByteArrayInputStream uploadConsumptionData(MultipartFile file);

  List<HourlyConsumptionDTO> getConsumptionByDate(LocalDate date);

  List<DailyConsumptionDTO> getConsumptionByMonth(LocalDate date);

  List<DailyConsumptionDTO> getConsumptionByWeek(LocalDate date);
}