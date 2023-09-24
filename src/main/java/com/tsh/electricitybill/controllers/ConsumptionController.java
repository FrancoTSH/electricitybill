package com.tsh.electricitybill.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tsh.electricitybill.dto.DailyConsumptionDTO;
import com.tsh.electricitybill.dto.HourlyConsumptionDTO;
import com.tsh.electricitybill.services.IConsumptionService;
import com.tsh.electricitybill.utils.ExcelUtils;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/consumption")
public class ConsumptionController {

  private IConsumptionService consumptionService;

  @PostMapping("/import")
  public ResponseEntity<?> importData(@RequestParam("file") MultipartFile file) {

    if (!ExcelUtils.hasExcelFormat(file)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload an excel file!");
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultado.xlsx");
    return ResponseEntity.ok().headers(headers)
        .body(new InputStreamResource(consumptionService.uploadConsumptionData(file)));
  }

  @GetMapping("/daily/{date}")
  public ResponseEntity<List<HourlyConsumptionDTO>> getDaily(@PathVariable("date") LocalDate date) {
    return ResponseEntity.ok(consumptionService.getConsumptionByDate(date));
  }

  @GetMapping("/monthly/{date}")
  public ResponseEntity<List<DailyConsumptionDTO>> getMonthly(@PathVariable("date") LocalDate date) {
    return ResponseEntity.ok(consumptionService.getConsumptionByMonth(date));
  }

  @GetMapping("/weekly/{date}")
  public ResponseEntity<List<DailyConsumptionDTO>> getWeekly(@PathVariable("date") LocalDate date) {
    return ResponseEntity.ok(consumptionService.getConsumptionByWeek(date));
  }
}
