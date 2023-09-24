package com.tsh.electricitybill.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.web.multipart.MultipartFile;

public class ExcelUtils {
  public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  public static boolean hasExcelFormat(MultipartFile file) {
    return TYPE.equals(file.getContentType());
  }

  public static boolean isCellEmpty(Cell cell) {
    return cell == null || cell.getCellType() == CellType.BLANK;
  }
}
