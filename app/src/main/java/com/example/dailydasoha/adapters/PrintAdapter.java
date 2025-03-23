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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrintAdapter extends PrintDocumentAdapter {
    private Context context;
    private FirebaseFirestore db;
    private List<DocumentSnapshot> data;
    private static final int ITEMS_PER_PAGE = 20;

    public PrintAdapter(Context context, FirebaseFirestore db) {
        this.context = context;
        this.db = db;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                        CancellationSignal cancellationSignal,
                        LayoutResultCallback callback, Bundle extras) {

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        // Set page size for landscape orientation
        PrintAttributes.MediaSize mediaSize = newAttributes.getMediaSize();
        if (mediaSize != null && mediaSize.isPortrait()) {
            newAttributes = new PrintAttributes.Builder()
                    .setMediaSize(mediaSize.asLandscape())
                    .build();
        }

        // Fetch data from Firestore
        db.collection("daily_data")
            .orderBy("date")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                data = queryDocumentSnapshots.getDocuments();
                
                // Calculate number of pages needed
                int pageCount = (int) Math.ceil(data.size() / (double) ITEMS_PER_PAGE);

                PrintDocumentInfo info = new PrintDocumentInfo.Builder("DailyDasoha_Ledger")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(pageCount)
                        .build();

                callback.onLayoutFinished(info, true);
            })
            .addOnFailureListener(e -> callback.onLayoutFailed("Error fetching data: " + e.getMessage()));
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                       CancellationSignal cancellationSignal,
                       WriteResultCallback callback) {
        
        if (data == null || data.isEmpty()) {
            callback.onWriteFailed("No data to print");
            return;
        }

        try {
            PdfDocument document = new PdfDocument();

            // Set page size for landscape orientation
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(842, 595, 0).create();

            for (int i = 0; i < pages.length; i++) {
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    document.close();
                    return;
                }

                PdfDocument.Page page = document.startPage(pageInfo);
                drawPage(page.getCanvas(), i);
                document.finishPage(page);
            }

            document.writeTo(new FileOutputStream(destination.getFileDescriptor()));
            document.close();
            callback.onWriteFinished(pages);

        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
        }
    }

    private void drawPage(Canvas canvas, int pageNum) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(8);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(0.5f);
        linePaint.setStyle(Paint.Style.STROKE);

        int startY = 50;
        int x = 20;
        int lineHeight = 20;
        int rowHeight = 25;

        int startIndex = pageNum * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, data.size());

        paint.setFakeBoldText(true);
        String[] headers = {
            "Date", "Working Day", 
            "Students (1-5)", "Students (6-8)", "Students (9-10)",
            "Milk (1-5)", "Rice (1-5)", "Wheat (1-5)", "Dhal (1-5)", "Oil (1-5)", "Salt (1-5)",
            "Milk (6-8)", "Rice (6-8)", "Wheat (6-8)", "Dhal (6-8)", "Oil (6-8)", "Salt (6-8)",
            "Milk (9-10)", "Rice (9-10)", "Wheat (9-10)", "Dhal (9-10)", "Oil (9-10)", "Salt (9-10)"
        };
        
        int[] columnWidths = {
            50, 40,  // Date, Working Day
            40, 40, 40,  // Students
            30, 30, 30, 30, 30, 30,  // Class 1-5 inventory
            30, 30, 30, 30, 30, 30,  // Class 6-8 inventory
            30, 30, 30, 30, 30, 30   // Class 9-10 inventory
        };

        int totalWidth = x;
        for (int width : columnWidths) {
            totalWidth += width;
        }

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.LTGRAY);
        canvas.drawRect(x, startY - 15, totalWidth, startY + 5, headerBgPaint);

        int currentX = x;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], currentX + 2, startY, paint);
            canvas.drawLine(currentX, startY - 15, currentX, startY + ((endIndex - startIndex + 1) * rowHeight), linePaint);
            currentX += columnWidths[i];
        }
        canvas.drawLine(currentX, startY - 15, currentX, startY + ((endIndex - startIndex + 1) * rowHeight), linePaint);

        canvas.drawLine(x, startY + 5, totalWidth, startY + 5, linePaint);

        paint.setFakeBoldText(false);
        startY += lineHeight;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (int i = startIndex; i < endIndex; i++) {
            DocumentSnapshot doc = data.get(i);
            currentX = x;

            canvas.drawLine(x, startY - 5, totalWidth, startY - 5, linePaint);

            canvas.drawText(sdf.format(new Date(doc.getLong("date"))), currentX + 2, startY, paint);
            currentX += columnWidths[0];
            canvas.drawText(doc.getBoolean("isWorkingDay") ? "Yes" : "No", currentX + 2, startY, paint);
            currentX += columnWidths[1];

            // Students
            canvas.drawText(String.valueOf(doc.getLong("attendance1to5")), currentX + 2, startY, paint);
            currentX += columnWidths[2];
            canvas.drawText(String.valueOf(doc.getLong("attendance6to8")), currentX + 2, startY, paint);
            currentX += columnWidths[3];
            canvas.drawText(String.valueOf(doc.getLong("attendance9to10")), currentX + 2, startY, paint);
            currentX += columnWidths[4];

            // Class 1-5 Inventory
            canvas.drawText(String.valueOf(doc.getLong("milk15")), currentX + 2, startY, paint);
            currentX += columnWidths[5];
            canvas.drawText(String.valueOf(doc.getLong("rice15")), currentX + 2, startY, paint);
            currentX += columnWidths[6];
            canvas.drawText(String.valueOf(doc.getLong("wheat15")), currentX + 2, startY, paint);
            currentX += columnWidths[7];
            canvas.drawText(String.valueOf(doc.getLong("dhal15")), currentX + 2, startY, paint);
            currentX += columnWidths[8];
            canvas.drawText(String.valueOf(doc.getLong("oil15")), currentX + 2, startY, paint);
            currentX += columnWidths[9];
            canvas.drawText(String.valueOf(doc.getLong("salt15")), currentX + 2, startY, paint);
            currentX += columnWidths[10];

            // Class 6-8 Inventory
            canvas.drawText(String.valueOf(doc.getLong("milk68")), currentX + 2, startY, paint);
            currentX += columnWidths[11];
            canvas.drawText(String.valueOf(doc.getLong("rice68")), currentX + 2, startY, paint);
            currentX += columnWidths[12];
            canvas.drawText(String.valueOf(doc.getLong("wheat68")), currentX + 2, startY, paint);
            currentX += columnWidths[13];
            canvas.drawText(String.valueOf(doc.getLong("dhal68")), currentX + 2, startY, paint);
            currentX += columnWidths[14];
            canvas.drawText(String.valueOf(doc.getLong("oil68")), currentX + 2, startY, paint);
            currentX += columnWidths[15];
            canvas.drawText(String.valueOf(doc.getLong("salt68")), currentX + 2, startY, paint);
            currentX += columnWidths[16];

            // Class 9-10 Inventory
            canvas.drawText(String.valueOf(doc.getLong("milk910")), currentX + 2, startY, paint);
            currentX += columnWidths[17];
            canvas.drawText(String.valueOf(doc.getLong("rice910")), currentX + 2, startY, paint);
            currentX += columnWidths[18];
            canvas.drawText(String.valueOf(doc.getLong("wheat910")), currentX + 2, startY, paint);
            currentX += columnWidths[19];
            canvas.drawText(String.valueOf(doc.getLong("dhal910")), currentX + 2, startY, paint);
            currentX += columnWidths[20];
            canvas.drawText(String.valueOf(doc.getLong("oil910")), currentX + 2, startY, paint);
            currentX += columnWidths[21];
            canvas.drawText(String.valueOf(doc.getLong("salt910")), currentX + 2, startY, paint);

            startY += rowHeight;
        }

        canvas.drawLine(x, startY - 5, totalWidth, startY - 5, linePaint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Page " + (pageNum + 1), canvas.getWidth() - 30, canvas.getHeight() - 30, paint);
    }
} 