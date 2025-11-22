package com.furtim.entitleguard.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.BillMaterialDto;
import com.furtim.entitleguard.entity.BillOfMaterials;
import com.furtim.entitleguard.entity.BuilderItem;
import com.furtim.entitleguard.entity.BuilderOrganization;
import com.furtim.entitleguard.repository.BillOfMaterialRepository;
import com.furtim.entitleguard.repository.BuilderItemRepository;
import com.furtim.entitleguard.repository.BuilderOrganizationRepository;
import com.furtim.entitleguard.repository.CategoryRepository;
import com.furtim.entitleguard.response.DefaultListResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
public class ExcelController {

	private final CategoryRepository categoryRepo;

	private final BillOfMaterialRepository billOfMaterialRepo;
	
	private final BuilderItemRepository builderItemRepo;
	
	private final BuilderOrganizationRepository builderOrganizationRepo;

	@GetMapping(value = "/auth/download-template")
	public void downloadTemplate(HttpServletResponse response) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Items");

		String[] headers = { "Name", "Category", "Make", "Brand", "Model", "Description", "Price", "Documentation_Url",
				"Notes", "Purchaser" };
		Row headerRow = sheet.createRow(0);

		for (int i = 0; i < headers.length; i++) {
			headerRow.createCell(i).setCellValue(headers[i]);
		}

		List<String> categories = categoryRepo.findByIsActive(true);

		createDropDown(sheet, categories.toArray(new String[0]), 1, 200, 1);

		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}

		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=builder_template.xlsx");

		workbook.write(response.getOutputStream());
		workbook.close();
	}

	private void createDropDown(XSSFSheet sheet, String[] values, int firstRow, int lastRow, int col) {
		DataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
		DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(values);
		CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, col, col);
		DataValidation validation = validationHelper.createValidation(constraint, addressList);
		validation.setSuppressDropDownArrow(true);
		validation.setShowErrorBox(true);
		sheet.addValidationData(validation);
	}

	@PostMapping(value = "/api/upload-template", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DefaultListResponse uploadTemplate(@ModelAttribute BillMaterialDto billMaterialDto) {
		try (InputStream is = billMaterialDto.getFile().getInputStream();
				XSSFWorkbook workbook = new XSSFWorkbook(is)) {

			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			if (rows.hasNext())
				rows.next();

			BillOfMaterials bill = new BillOfMaterials();
			bill.setBomName(billMaterialDto.getBomName());
			bill.setProjectName(billMaterialDto.getProjectName());
			billOfMaterialRepo.save(bill);

			while (rows.hasNext()) {
				Row row = rows.next();

				String name = getCellValue(row.getCell(0));
				String category = getCellValue(row.getCell(1));
				String make = getCellValue(row.getCell(2));
				String brand = getCellValue(row.getCell(3));
				String model = getCellValue(row.getCell(4));
				String description = getCellValue(row.getCell(5));
				String price = getCellValue(row.getCell(6));
				String documentationUrl = getCellValue(row.getCell(7));
				String notes = getCellValue(row.getCell(8));
				String purchaser = getCellValue(row.getCell(9));

				if (name == null || name.isBlank())
					continue;
				if (category == null || category.isBlank())
					continue;

				BuilderItem item = new BuilderItem();
				item.setBillOfMaterials(bill);
				item.setName(name);
				item.setCategory(category);
				item.setMake(make);
				item.setBrand(brand);
				item.setModel(model);
				item.setText(description);
				item.setPrice(price);
				item.setDocumentationUrl(documentationUrl);
				item.setNote(notes);
				item.setPuchaser(purchaser);
				Optional<BuilderOrganization> builder = builderOrganizationRepo.findOneById(billMaterialDto.getBuilderOrganizationId());
				builder.ifPresent(item::setBuilderOrganization);
				builderItemRepo.save(item);

			}
			return new DefaultListResponse(true, "Excel Uploaded Successfully", null);
		} catch (IOException e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong", null);
		}

	}

	private String getCellValue(Cell cell) {
	    if (cell == null) return "";

	    switch (cell.getCellType()) {
	        case STRING:
	            return cell.getStringCellValue().trim();
	        case NUMERIC:
	            return String.valueOf(cell.getNumericCellValue());
	        case BOOLEAN:
	            return String.valueOf(cell.getBooleanCellValue());
	        case FORMULA:
	            try {
	                return cell.getStringCellValue();
	            } catch (Exception e) {
	                return String.valueOf(cell.getNumericCellValue());
	            }
	        default:
	            return "";
	    }
	}


}
