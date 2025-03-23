package com.example.dailydasoha.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.dailydasoha.models.DailyData;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportUtil {
    public static void exportToExcel(Context context, FirebaseFirestore db) {
        db.collection("daily_data")
            .orderBy("date")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Workbook workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet("Daily Data");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Date");
                headerRow.createCell(1).setCellValue("Working Day");
                headerRow.createCell(2).setCellValue("Class 1-5");
                headerRow.createCell(3).setCellValue("Class 6-8");
                headerRow.createCell(4).setCellValue("Class 9-10");
                headerRow.createCell(5).setCellValue("Grain Type");
                headerRow.createCell(6).setCellValue("Total Attendance");

                int rowNum = 1;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    DailyData data = document.toObject(DailyData.class);
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(sdf.format(new Date(data.getDate())));
                    row.createCell(1).setCellValue(data.isWorkingDay() ? "Yes" : "No");
                    row.createCell(2).setCellValue(data.getAttendance1to5());
                    row.createCell(3).setCellValue(data.getAttendance6to8());
                    row.createCell(4).setCellValue(data.getAttendance9to10());
                    row.createCell(5).setCellValue(data.getGrainType());
                    row.createCell(6).setCellValue(data.getTotalAttendance());
                }

                // Auto size columns
                for (int i = 0; i < 7; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write the workbook to a file
                try {
                    String fileName = "DailyDasoha_" + 
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(new Date()) + ".xls";
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File file = new File(downloadsDir, fileName);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    workbook.write(outputStream);
                    outputStream.close();
                    Toast.makeText(context, "Excel file saved to Downloads folder: " + fileName, 
                        Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Error exporting to Excel: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(context, 
                "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public static void exportToPDF(Context context, FirebaseFirestore db) {
        db.collection("daily_data")
            .orderBy("date")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Document document = new Document();
                try {
                    String fileName = "DailyDasoha_" + 
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(new Date()) + ".pdf";
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File file = new File(downloadsDir, fileName);
                    PdfWriter.getInstance(document, new FileOutputStream(file));
                    document.open();

                    // Add title
                    com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                        com.itextpdf.text.Font.FontFamily.HELVETICA, 18, 
                        com.itextpdf.text.Font.BOLD);
                    Paragraph title = new Paragraph("Daily Dasoha Report", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    document.add(title);
                    document.add(new Paragraph("\n"));

                    // Create table
                    PdfPTable table = new PdfPTable(7);
                    table.setWidthPercentage(100);

                    // Add header row
                    addTableHeader(table);

                    // Add data rows
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        DailyData data = doc.toObject(DailyData.class);
                        addTableRow(table, data, sdf);
                    }

                    document.add(table);
                    document.close();

                    Toast.makeText(context, "PDF file saved to Downloads folder: " + fileName, 
                        Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, "Error exporting to PDF: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(context, 
                "Error fetching data: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private static void addTableHeader(PdfPTable table) {
        String[] headers = {"Date", "Working Day", "Class 1-5", "Class 6-8", 
            "Class 9-10", "Grain Type", "Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private static void addTableRow(PdfPTable table, DailyData data, SimpleDateFormat sdf) {
        table.addCell(sdf.format(new Date(data.getDate())));
        table.addCell(data.isWorkingDay() ? "Yes" : "No");
        table.addCell(String.valueOf(data.getAttendance1to5()));
        table.addCell(String.valueOf(data.getAttendance6to8()));
        table.addCell(String.valueOf(data.getAttendance9to10()));
        table.addCell(data.getGrainType());
        table.addCell(String.valueOf(data.getTotalAttendance()));
    }
} 