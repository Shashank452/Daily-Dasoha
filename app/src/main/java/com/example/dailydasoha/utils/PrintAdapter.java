package com.example.dailydasoha.utils;

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
import android.print.pdf.PrintedPdfDocument;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.dailydasoha.models.DailyData;

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
    private PrintedPdfDocument pdfDocument;
    private List<DailyData> data;

    public PrintAdapter(Context context, FirebaseFirestore db) {
        this.context = context;
        this.db = db;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                        CancellationSignal cancellationSignal,
                        LayoutResultCallback callback, Bundle extras) {
        
        // Create a new PdfDocument with the requested page attributes
        pdfDocument = new PrintedPdfDocument(context, newAttributes);

        // If cancellation signal is triggered, abort the print job
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        // Fetch data from Firestore
        db.collection("daily_data")
            .orderBy("date")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                data = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    data.add(document.toObject(DailyData.class));
                }

                // Calculate number of pages needed
                int pageCount = (int) Math.ceil(data.size() / 20.0); // 20 items per page

                PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                    .Builder("DailyDasoha_Report")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(pageCount);

                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            })
            .addOnFailureListener(e -> callback.onLayoutFailed("Error fetching data: " + e.getMessage()));
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                       CancellationSignal cancellationSignal,
                       WriteResultCallback callback) {
        
        // Start creating the printed document
        for (int i = 0; i < pages.length; i++) {
            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                return;
            }

            // Start a page
            PdfDocument.Page page = pdfDocument.startPage(i);
            Canvas canvas = page.getCanvas();

            // Draw content
            drawPage(canvas, i);

            // Finish the page
            pdfDocument.finishPage(page);
        }

        try {
            // Write the document content
            pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
            callback.onWriteFinished(pages);
        } catch (IOException e) {
            callback.onWriteFailed(e.getMessage());
        } finally {
            pdfDocument.close();
        }
    }

    private void drawPage(Canvas canvas, int pageNum) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        // Draw header
        int y = 50;
        int x = 50;
        String[] headers = {"Date", "Working Day", "Class 1-5", "Class 6-8", "Class 9-10", "Grain Type", "Total"};
        for (String header : headers) {
            canvas.drawText(header, x, y, paint);
            x += 100;
        }

        // Draw data rows
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        int startIndex = pageNum * 20;
        int endIndex = Math.min(startIndex + 20, data.size());

        for (int i = startIndex; i < endIndex; i++) {
            y += 30;
            x = 50;
            DailyData item = data.get(i);

            canvas.drawText(sdf.format(new Date(item.getDate())), x, y, paint);
            x += 100;
            canvas.drawText(item.isWorkingDay() ? "Yes" : "No", x, y, paint);
            x += 100;
            canvas.drawText(String.valueOf(item.getAttendance1to5()), x, y, paint);
            x += 100;
            canvas.drawText(String.valueOf(item.getAttendance6to8()), x, y, paint);
            x += 100;
            canvas.drawText(String.valueOf(item.getAttendance9to10()), x, y, paint);
            x += 100;
            canvas.drawText(item.getGrainType(), x, y, paint);
            x += 100;
            canvas.drawText(String.valueOf(item.getTotalAttendance()), x, y, paint);
        }
    }
} 