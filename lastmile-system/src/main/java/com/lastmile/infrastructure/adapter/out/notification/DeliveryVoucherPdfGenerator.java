package com.lastmile.infrastructure.adapter.out.notification;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.lastmile.domain.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DeliveryVoucherPdfGenerator {

    private static final DeviceRgb DARK_BG    = new DeviceRgb(26, 26, 46);
    private static final DeviceRgb GREEN      = new DeviceRgb(22, 163, 74);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb GRAY_TEXT  = new DeviceRgb(100, 100, 120);
    private static final DeviceRgb BORDER     = new DeviceRgb(220, 220, 230);

    public byte[] generate(Order order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A5);
            document.setMargins(36, 36, 36, 36);

            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont mono    = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);

            // ── HEADER ──────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell headerCell = new Cell()
                    .setBackgroundColor(DARK_BG)
                    .setPadding(20)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph("LAST MILE DELIVERY")
                            .setFont(regular).setFontSize(9)
                            .setFontColor(new DeviceRgb(160, 160, 176))
                            .setCharacterSpacing(3)
                            .setMarginBottom(4))
                    .add(new Paragraph("COMPROBANTE DE ENTREGA")
                            .setFont(bold).setFontSize(16)
                            .setFontColor(ColorConstants.WHITE)
                            .setMarginBottom(10))
                    .add(new Paragraph("✓  ENTREGA EXITOSA")
                            .setFont(bold).setFontSize(10)
                            .setFontColor(ColorConstants.WHITE)
                            .setBackgroundColor(GREEN)
                            .setPaddingLeft(16).setPaddingRight(16)
                            .setPaddingTop(6).setPaddingBottom(6));
            header.addCell(headerCell);
            document.add(header);

            // ── TRACKING CODE ────────────────────────────────────
            Table trackingSection = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            Cell trackingCell = new Cell()
                    .setBackgroundColor(LIGHT_GRAY)
                    .setPadding(16)
                    .setBorder(new SolidBorder(BORDER, 1))
                    .setTextAlignment(TextAlignment.CENTER)
                    .add(new Paragraph("CÓDIGO DE SEGUIMIENTO")
                            .setFont(regular).setFontSize(8)
                            .setFontColor(GRAY_TEXT)
                            .setCharacterSpacing(2)
                            .setMarginBottom(6))
                    .add(new Paragraph(order.getTrackingCode())
                            .setFont(mono).setFontSize(18)
                            .setFontColor(DARK_BG)
                            .setCharacterSpacing(2));
            trackingSection.addCell(trackingCell);
            document.add(trackingSection);

            // ── DETAILS TABLE ────────────────────────────────────
            document.add(new Paragraph(" "));

            Table details = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();

            addDetailRow(details, "Destinatario",    order.getRecipientName(),          bold, regular);
            addDetailRow(details, "Teléfono",        order.getRecipientPhone(),          bold, regular);
            addDetailRow(details, "Dirección",       order.getAddressText(),             bold, regular);
            addDetailRow(details, "Fecha entrega",   LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),                    bold, regular);
            addDetailRow(details, "Prioridad",       order.getPriority().name(),         bold, regular);
            addDetailRow(details, "Estado",          "ENTREGADO",                        bold, regular);

            document.add(details);

            // ── SEPARATOR ────────────────────────────────────────
            document.add(new Paragraph(" "));
            SolidLine line = new SolidLine(1f);
            line.setColor(BORDER);
            document.add(new LineSeparator(line));
            document.add(new Paragraph(" "));

            // ── FOOTER ───────────────────────────────────────────
            document.add(new Paragraph("Gracias por confiar en Last Mile Delivery.")
                    .setFont(regular).setFontSize(10)
                    .setFontColor(GRAY_TEXT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));
            document.add(new Paragraph("© 2026 Last Mile Delivery — Todos los derechos reservados")
                    .setFont(regular).setFontSize(8)
                    .setFontColor(new DeviceRgb(180, 180, 190))
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating voucher PDF for order {}: {}", order.getTrackingCode(), e.getMessage());
            return new byte[0];
        }
    }

    private void addDetailRow(Table table, String label, String value, PdfFont bold, PdfFont regular) {
        table.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDER, 0.5f))
                .setPadding(10)
                .setBackgroundColor(LIGHT_GRAY)
                .add(new Paragraph(label)
                        .setFont(bold).setFontSize(9)
                        .setFontColor(GRAY_TEXT)));
        table.addCell(new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDER, 0.5f))
                .setPadding(10)
                .add(new Paragraph(value != null ? value : "-")
                        .setFont(regular).setFontSize(10)
                        .setFontColor(DARK_BG)));
    }
}