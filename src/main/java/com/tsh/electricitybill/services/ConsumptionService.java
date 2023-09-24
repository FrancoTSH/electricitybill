package com.tsh.electricitybill.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.ByteArrayInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tsh.electricitybill.dto.DailyConsumptionDTO;
import com.tsh.electricitybill.dto.HourlyConsumptionDTO;
import com.tsh.electricitybill.entities.Consumption;
import com.tsh.electricitybill.repositories.IConsumptionRepository;
import com.tsh.electricitybill.utils.ExcelUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConsumptionService implements IConsumptionService {

  private IConsumptionRepository consumptionRepository;

  @Override
  public ByteArrayInputStream uploadConsumptionData(MultipartFile file) {
    try {
      List<Consumption> list = new ArrayList<Consumption>();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Workbook workbook = new XSSFWorkbook(file.getInputStream());

      Sheet sheet = workbook.getSheetAt(0);
      for (int index = 0; index < sheet.getPhysicalNumberOfRows(); index++) {
        Row row = sheet.getRow(index);

        if (index > 0) {
          Consumption consumption = new Consumption();

          Cell activeEnergyCell = row.getCell(1);
          Cell meterDateCell = row.getCell(2);
          Cell meterTimeCell = row.getCell(3);

          if (ExcelUtils.isCellEmpty(activeEnergyCell) || ExcelUtils.isCellEmpty(meterDateCell)
              || ExcelUtils.isCellEmpty(meterTimeCell)) {
            row.createCell(4).setCellValue("Error: Existe un campo vacio");
            continue;
          }

          consumption.setActiveEnergy(activeEnergyCell.getNumericCellValue());
          consumption
              .setMeterDate(meterDateCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
          consumption
              .setMeterTime(meterTimeCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalTime());

          list.add(consumption);
          row.createCell(4).setCellValue("OK");
        } else {
          row.createCell(4).setCellValue("incidencia");
        }
      }
      workbook.write(out);
      workbook.close();
      this.consumptionRepository.saveAll(list);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("Fail to import data from Excel file: " + e.getMessage());
    }
  }

  @Override
  public List<HourlyConsumptionDTO> getConsumptionByDate(LocalDate date) {
    List<HourlyConsumptionDTO> list = new ArrayList<>();

    List<Consumption> consumptions = consumptionRepository.findByMeterDateBetween(date, date);

    for (int i = 0; i < 24; i++) {
      LocalTime time = LocalTime.of(i, 0);

      List<Consumption> consumptionsAtHour = consumptions.stream()
          .filter(consumption -> consumption.getMeterTime().getHour() == time.getHour())
          .sorted((a, b) -> a.getMeterTime().compareTo(b.getMeterTime())).toList();

      Double energyConsumptionAtHour = 0d;

      if (!consumptionsAtHour.isEmpty()) {
        Double minConsumption = consumptionsAtHour.get(0).getActiveEnergy();
        Double maxConsumption = consumptionsAtHour.get(consumptionsAtHour.size() - 1).getActiveEnergy();

        energyConsumptionAtHour = maxConsumption - minConsumption;
      }

      HourlyConsumptionDTO hourConsumption = HourlyConsumptionDTO.builder()
          .time(time.format(DateTimeFormatter.ofPattern("HH:mm")))
          .energy(BigDecimal.valueOf(energyConsumptionAtHour).setScale(2, RoundingMode.HALF_UP).doubleValue()).build();

      list.add(hourConsumption);
    }

    return list;
  }

  @Override
  public List<DailyConsumptionDTO> getConsumptionByMonth(LocalDate date) {
    LocalDate firstDateOfMonth = date.withDayOfMonth(1);
    LocalDate lastDateOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
    List<DailyConsumptionDTO> list = new ArrayList<>();

    List<Consumption> consumptions = consumptionRepository.findByMeterDateBetween(firstDateOfMonth,
        lastDateOfMonth);

    firstDateOfMonth.datesUntil(lastDateOfMonth).forEach(day -> {
      Double energyConsumptionAtDay = 0d;
      List<Consumption> consumptionsAtDay = consumptions.stream()
          .filter(consumption -> consumption.getMeterDate().isEqual(day))
          .sorted((a, b) -> a.getMeterTime().compareTo(b.getMeterTime())).toList();

      if (!consumptionsAtDay.isEmpty()) {
        Double minConsumption = consumptionsAtDay.get(0).getActiveEnergy();
        Double maxConsumption = consumptionsAtDay.get(consumptionsAtDay.size() - 1).getActiveEnergy();

        energyConsumptionAtDay = maxConsumption - minConsumption;
      }

      DailyConsumptionDTO hourConsumption = DailyConsumptionDTO.builder()
          .date(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
          .energy(BigDecimal.valueOf(energyConsumptionAtDay).setScale(2, RoundingMode.HALF_UP).doubleValue()).build();

      list.add(hourConsumption);
    });

    return list;
  }

  @Override
  public List<DailyConsumptionDTO> getConsumptionByWeek(LocalDate date) {
    LocalDate firstDayOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    LocalDate lastDateOfWeek = firstDayOfWeek.plusDays(7);
    List<DailyConsumptionDTO> list = new ArrayList<>();

    List<Consumption> consumptions = consumptionRepository.findByMeterDateBetween(firstDayOfWeek,
        lastDateOfWeek);

    firstDayOfWeek.datesUntil(lastDateOfWeek).forEach(day -> {
      Double energyConsumptionAtDay = 0d;
      List<Consumption> consumptionsAtDay = consumptions.stream()
          .filter(consumption -> consumption.getMeterDate().isEqual(day))
          .sorted((a, b) -> a.getMeterTime().compareTo(b.getMeterTime())).toList();

      if (!consumptionsAtDay.isEmpty()) {
        Double minConsumption = consumptionsAtDay.get(0).getActiveEnergy();
        Double maxConsumption = consumptionsAtDay.get(consumptionsAtDay.size() - 1).getActiveEnergy();

        energyConsumptionAtDay = maxConsumption - minConsumption;
      }

      DailyConsumptionDTO hourConsumption = DailyConsumptionDTO.builder()
          .date(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
          .energy(BigDecimal.valueOf(energyConsumptionAtDay).setScale(2, RoundingMode.HALF_UP).doubleValue()).build();

      list.add(hourConsumption);
    });

    return list;
  }

}
