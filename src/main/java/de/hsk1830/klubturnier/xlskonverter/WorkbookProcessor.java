package de.hsk1830.klubturnier.xlskonverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Maps;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Created by kleinr on 02/06/2014.
 */
public class WorkbookProcessor {
	private final XSSFWorkbook workbook;

	WorkbookProcessor(final File workbookFile) throws IOException {
		workbook = new XSSFWorkbook(new FileInputStream(Objects.requireNonNull(workbookFile)));
	}

	private Sheet getKlasseSheet() {
		return workbook.getSheet("Klasse");
	}

	private static String getStringCellContent(final Cell cell) {
		final String cellValue;
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:
				cellValue = Integer.valueOf((int) cell.getNumericCellValue()).toString();
				break;
			case Cell.CELL_TYPE_FORMULA:
			case Cell.CELL_TYPE_STRING:
			case Cell.CELL_TYPE_BLANK:
				cellValue = cell.getStringCellValue();
				break;
			default:
				throw new IllegalArgumentException(String.format("Kann Cell Type %1d nicht interpretieren.", cell.getCellType()));
		}
		return cellValue;
	}

	List<Spieler> getSpielerList(final List<String> tableColumnHeaders) {
		final Map<String, Integer> klasseColumnMap = getKlasseColumnMap(tableColumnHeaders);
		return StreamSupport.stream(getKlasseSheet().spliterator(), false).filter(row -> row.getRowNum() != 0).map(row -> toSpieler(row, klasseColumnMap, klasseColumnMap.get("Vorname"), klasseColumnMap.get("Nachname"))).collect(Collectors.toList());
	}

	private static Spieler toSpieler(final Row row, final Map<String, Integer> klasseColumnMap, final int klasseVornameIndex, final int klasseNachnameIndex) {
		final Spieler spieler = new Spieler(getStringCellContent(row.getCell(klasseVornameIndex)), getStringCellContent(row.getCell(klasseNachnameIndex)));
		klasseColumnMap.entrySet().forEach(klasseColumnEntry -> {
			spieler.putAttributeValue(klasseColumnEntry.getKey(), getStringCellContent(row.getCell(klasseColumnEntry.getValue())));
		});
		return spieler;
	}

	private Map<String, Integer> getKlasseColumnMap(final List<String> tableColumnHeaders) {
		final Map<String, Integer> klasseColumnMap = Maps.newLinkedHashMap();
		getKlasseSheet().getRow(0).forEach(cell -> {
			final String cellContent = getStringCellContent(cell);
			if (tableColumnHeaders.contains(cellContent)) {
				klasseColumnMap.put(cellContent, cell.getColumnIndex());
			}
		});
		return klasseColumnMap;
	}
}
