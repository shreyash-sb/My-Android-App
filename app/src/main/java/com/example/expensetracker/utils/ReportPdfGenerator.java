package com.example.expensetracker.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import androidx.annotation.NonNull;

import com.example.expensetracker.models.Expense;
import com.example.expensetracker.models.Income;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class ReportPdfGenerator {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int PAGE_MARGIN = 36;
    private static final int ROW_HEIGHT = 22;

    private ReportPdfGenerator() {
    }

    @NonNull
    public static File generateFinanceReport(@NonNull Context context,
                                             @NonNull List<Expense> expenses,
                                             @NonNull List<Income> incomes,
                                             long fromDate,
                                             long toDate) throws IOException {
        File reportsDirectory = new File(context.getCacheDir(), "reports");
        if (!reportsDirectory.exists() && !reportsDirectory.mkdirs()) {
            throw new IOException("Unable to create report directory.");
        }

        File reportFile = new File(reportsDirectory, "smart-report-" + System.currentTimeMillis() + ".pdf");
        PdfDocument pdfDocument = new PdfDocument();

        Paint titlePaint = buildPaint(18f, true);
        Paint headingPaint = buildPaint(12f, true);
        Paint bodyPaint = buildPaint(10f, false);
        Paint linePaint = buildPaint(10f, false);
        linePaint.setStrokeWidth(1f);

        PageState pageState = newPage(pdfDocument, 1);
        int currentY = drawHeader(pageState.canvas, titlePaint, headingPaint, bodyPaint, expenses, incomes, fromDate, toDate);

        currentY = ensureSpace(pdfDocument, pageState, currentY, 34, fromDate, toDate, headingPaint, bodyPaint, linePaint);
        currentY = drawSectionTitle(pageState.canvas, headingPaint, "Expenses", currentY);
        currentY = drawTableHeader(pageState.canvas, headingPaint, linePaint, currentY, "Category", "Note");
        if (expenses.isEmpty()) {
            currentY = drawInfoLine(pageState.canvas, bodyPaint, "No expenses found in this range.", currentY);
        } else {
            for (Expense expense : expenses) {
                currentY = ensureSpace(pdfDocument, pageState, currentY, ROW_HEIGHT + 8, fromDate, toDate, headingPaint, bodyPaint, linePaint);
                currentY = drawExpenseRow(pageState.canvas, bodyPaint, expense, currentY);
            }
        }

        currentY += 10;
        currentY = ensureSpace(pdfDocument, pageState, currentY, 34, fromDate, toDate, headingPaint, bodyPaint, linePaint);
        currentY = drawSectionTitle(pageState.canvas, headingPaint, "Income", currentY);
        currentY = drawTableHeader(pageState.canvas, headingPaint, linePaint, currentY, "Reason", "Type");
        if (incomes.isEmpty()) {
            drawInfoLine(pageState.canvas, bodyPaint, "No income entries found in this range.", currentY);
        } else {
            for (Income income : incomes) {
                currentY = ensureSpace(pdfDocument, pageState, currentY, ROW_HEIGHT + 8, fromDate, toDate, headingPaint, bodyPaint, linePaint);
                currentY = drawIncomeRow(pageState.canvas, bodyPaint, income, currentY);
            }
        }

        pdfDocument.finishPage(pageState.page);
        try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {
            pdfDocument.writeTo(outputStream);
        } finally {
            pdfDocument.close();
        }

        return reportFile;
    }

    @NonNull
    private static Paint buildPaint(float textSize, boolean isBold) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF171123);
        paint.setTextSize(textSize);
        paint.setFakeBoldText(isBold);
        return paint;
    }

    private static int drawHeader(Canvas canvas,
                                  Paint titlePaint,
                                  Paint headingPaint,
                                  Paint bodyPaint,
                                  List<Expense> expenses,
                                  List<Income> incomes,
                                  long fromDate,
                                  long toDate) {
        int y = PAGE_MARGIN + 8;
        canvas.drawText("Smart Expense Tracker", PAGE_MARGIN, y, headingPaint);
        y += 26;
        canvas.drawText("Smart Report", PAGE_MARGIN, y, titlePaint);
        y += 22;
        canvas.drawText("Range: " + FormatUtils.formatDate(fromDate) + " to " + FormatUtils.formatDate(toDate), PAGE_MARGIN, y, bodyPaint);
        y += 16;
        canvas.drawText("Generated: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()), PAGE_MARGIN, y, bodyPaint);
        y += 22;

        double totalExpense = 0;
        for (Expense expense : expenses) {
            totalExpense += expense.getAmount();
        }
        double totalIncome = 0;
        for (Income income : incomes) {
            totalIncome += income.getAmount();
        }

        canvas.drawText("Expense count: " + expenses.size(), PAGE_MARGIN, y, bodyPaint);
        y += 16;
        canvas.drawText("Total spending: " + FormatUtils.formatCurrency(totalExpense), PAGE_MARGIN, y, bodyPaint);
        y += 16;
        canvas.drawText("Income count: " + incomes.size(), PAGE_MARGIN, y, bodyPaint);
        y += 16;
        canvas.drawText("Total income: " + FormatUtils.formatCurrency(totalIncome), PAGE_MARGIN, y, bodyPaint);
        y += 16;
        canvas.drawText("Net balance: " + FormatUtils.formatCurrency(totalIncome - totalExpense), PAGE_MARGIN, y, bodyPaint);
        return y + 24;
    }

    private static int drawSectionTitle(Canvas canvas, Paint headingPaint, String title, int startY) {
        canvas.drawText(title, PAGE_MARGIN, startY, headingPaint);
        return startY + 14;
    }

    private static int drawTableHeader(Canvas canvas,
                                       Paint headingPaint,
                                       Paint linePaint,
                                       int startY,
                                       String secondColumnTitle,
                                       String thirdColumnTitle) {
        int y = startY;
        canvas.drawLine(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y, linePaint);
        y += 16;
        canvas.drawText("Date", PAGE_MARGIN, y, headingPaint);
        canvas.drawText(secondColumnTitle, PAGE_MARGIN + 95, y, headingPaint);
        canvas.drawText(thirdColumnTitle, PAGE_MARGIN + 255, y, headingPaint);
        canvas.drawText("Amount", PAGE_WIDTH - PAGE_MARGIN - 85, y, headingPaint);
        y += 8;
        canvas.drawLine(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y, linePaint);
        return y + 16;
    }

    private static int drawExpenseRow(Canvas canvas, Paint bodyPaint, Expense expense, int startY) {
        int y = startY;
        canvas.drawText(FormatUtils.formatDate(expense.getDate()), PAGE_MARGIN, y, bodyPaint);
        canvas.drawText(safeText(expense.getCategory(), 18), PAGE_MARGIN + 95, y, bodyPaint);
        canvas.drawText(safeText(expense.getNote(), 22), PAGE_MARGIN + 255, y, bodyPaint);
        canvas.drawText("-" + FormatUtils.formatCurrency(expense.getAmount()), PAGE_WIDTH - PAGE_MARGIN - 85, y, bodyPaint);
        return y + ROW_HEIGHT;
    }

    private static int drawIncomeRow(Canvas canvas, Paint bodyPaint, Income income, int startY) {
        int y = startY;
        canvas.drawText(FormatUtils.formatDate(income.getDate()), PAGE_MARGIN, y, bodyPaint);
        canvas.drawText(safeText(income.getReason(), 18), PAGE_MARGIN + 95, y, bodyPaint);
        canvas.drawText("Credit", PAGE_MARGIN + 255, y, bodyPaint);
        canvas.drawText("+" + FormatUtils.formatCurrency(income.getAmount()), PAGE_WIDTH - PAGE_MARGIN - 85, y, bodyPaint);
        return y + ROW_HEIGHT;
    }

    private static int drawInfoLine(Canvas canvas, Paint bodyPaint, String text, int startY) {
        canvas.drawText(text, PAGE_MARGIN, startY, bodyPaint);
        return startY + 20;
    }

    private static int ensureSpace(PdfDocument pdfDocument,
                                   PageState pageState,
                                   int currentY,
                                   int requiredHeight,
                                   long fromDate,
                                   long toDate,
                                   Paint headingPaint,
                                   Paint bodyPaint,
                                   Paint linePaint) {
        if (currentY + requiredHeight <= PAGE_HEIGHT - PAGE_MARGIN) {
            return currentY;
        }

        pdfDocument.finishPage(pageState.page);
        PageState newState = newPage(pdfDocument, pageState.pageNumber + 1);
        pageState.page = newState.page;
        pageState.canvas = newState.canvas;
        pageState.pageNumber = newState.pageNumber;

        int y = PAGE_MARGIN + 8;
        pageState.canvas.drawText("Smart Report", PAGE_MARGIN, y, headingPaint);
        y += 18;
        pageState.canvas.drawText("Range: " + FormatUtils.formatDate(fromDate) + " to " + FormatUtils.formatDate(toDate), PAGE_MARGIN, y, bodyPaint);
        y += 16;
        pageState.canvas.drawText("Page " + pageState.pageNumber, PAGE_MARGIN, y, bodyPaint);
        y += 12;
        pageState.canvas.drawLine(PAGE_MARGIN, y, PAGE_WIDTH - PAGE_MARGIN, y, linePaint);
        return y + 20;
    }

    @NonNull
    private static PageState newPage(PdfDocument pdfDocument, int pageNumber) {
        PdfDocument.Page page = pdfDocument.startPage(new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                pageNumber
        ).create());
        return new PageState(pageNumber, page, page.getCanvas());
    }

    @NonNull
    private static String safeText(String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength - 3) + "...";
    }

    private static final class PageState {
        private int pageNumber;
        private PdfDocument.Page page;
        private Canvas canvas;

        private PageState(int pageNumber, PdfDocument.Page page, Canvas canvas) {
            this.pageNumber = pageNumber;
            this.page = page;
            this.canvas = canvas;
        }
    }
}
