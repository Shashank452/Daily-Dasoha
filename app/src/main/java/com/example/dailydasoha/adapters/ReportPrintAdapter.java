package com.example.dailydasoha.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ReportPrintAdapter extends PrintDocumentAdapter {
    private Context context;
    private Map<String, Object> reportData;
    private String reportTitle;
    private String period;

    public ReportPrintAdapter(Context context, Map<String, Object> reportData, String reportTitle, String period) {
        this.context = context;
        this.reportData = reportData;
        this.reportTitle = reportTitle;
        this.period = period;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                        CancellationSignal cancellationSignal,
                        LayoutResultCallback callback, Bundle extras) {

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        PrintDocumentInfo info = new PrintDocumentInfo.Builder("report.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build();

        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                       CancellationSignal cancellationSignal,
                       WriteResultCallback callback) {
        
        try {
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 0).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            
            drawReport(page.getCanvas());
            
            document.finishPage(page);
            document.writeTo(new FileOutputStream(destination.getFileDescriptor()));
            document.close();
            
            callback.onWriteFinished(pages);
            
        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
        }
    }

    private void drawReport(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(0.5f);
        linePaint.setStyle(Paint.Style.STROKE);

        int startY = 50;
        int x = 40;
        int lineHeight = 25;

        // Draw Title
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText(reportTitle, x, startY, paint);
        startY += lineHeight;

        // Draw Period
        paint.setTextSize(12);
        canvas.drawText("Period: " + period, x, startY, paint);
        startY += lineHeight * 2;

        // Draw Table
        String[] headers = {
            "Category", "Class 1-5", "Class 6-8", "Class 9-10", "Total"
        };
        
        int[] columnWidths = {120, 100, 100, 100, 100};
        int totalWidth = x;
        for (int width : columnWidths) {
            totalWidth += width;
        }

        // Draw header background
        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.LTGRAY);
        canvas.drawRect(x, startY - 15, totalWidth, startY + 5, headerBgPaint);

        // Draw headers
        int currentX = x;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], currentX + 5, startY, paint);
            canvas.drawLine(currentX, startY - 15, currentX, startY + 200, linePaint);
            currentX += columnWidths[i];
        }
        canvas.drawLine(currentX, startY - 15, currentX, startY + 200, linePaint);

        // Draw horizontal line under header
        canvas.drawLine(x, startY + 5, totalWidth, startY + 5, linePaint);
        startY += lineHeight;

        // Draw data rows
        paint.setFakeBoldText(false);
        String[] categories = {"Working Days", "Students", "Milk (ml)", "Rice (g)", "Wheat (g)", 
                             "Dhal (g)", "Oil (ml)", "Salt (g)"};

        for (String category : categories) {
            currentX = x;
            
            // Draw horizontal line
            canvas.drawLine(x, startY - 5, totalWidth, startY - 5, linePaint);

            // Category
            canvas.drawText(category, currentX + 5, startY, paint);
            currentX += columnWidths[0];

            // Class 1-5
            String key15 = category.toLowerCase().replace(" ", "") + "15";
            canvas.drawText(String.valueOf(reportData.getOrDefault(key15, 0)), currentX + 5, startY, paint);
            currentX += columnWidths[1];

            // Class 6-8
            String key68 = category.toLowerCase().replace(" ", "") + "68";
            canvas.drawText(String.valueOf(reportData.getOrDefault(key68, 0)), currentX + 5, startY, paint);
            currentX += columnWidths[2];

            // Class 9-10
            String key910 = category.toLowerCase().replace(" ", "") + "910";
            canvas.drawText(String.valueOf(reportData.getOrDefault(key910, 0)), currentX + 5, startY, paint);
            currentX += columnWidths[3];

            // Total
            int total = Integer.parseInt(String.valueOf(reportData.getOrDefault(key15, 0))) +
                       Integer.parseInt(String.valueOf(reportData.getOrDefault(key68, 0))) +
                       Integer.parseInt(String.valueOf(reportData.getOrDefault(key910, 0)));
            canvas.drawText(String.valueOf(total), currentX + 5, startY, paint);

            startY += lineHeight;
        }

        // Draw final horizontal line
        canvas.drawLine(x, startY - 5, totalWidth, startY - 5, linePaint);
    }
} 