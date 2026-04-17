package com.frameasy.service;

import com.frameasy.model.Agreement;
import com.frameasy.model.User;
import com.frameasy.repository.UserRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Generate legal agreement PDF using iText 7.
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private final UserRepository userRepository;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public byte[] generateAgreementPdf(Agreement agreement, String itemTitle, String itemDetails) throws Exception {
        User seller = userRepository.findById(agreement.getSellerId()).orElse(null);
        User buyer = userRepository.findById(agreement.getBuyerId()).orElse(null);
        if (seller == null || buyer == null) return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("FARM EASY - LEGAL AGREEMENT")
                .setFontSize(18)
                .setBold()
                .setFontColor(ColorConstants.GREEN));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Agreement Type: " + agreement.getAgreementType()).setFontSize(12));
        document.add(new Paragraph("Date: " + agreement.getSignedAt().atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE)).setFontSize(12));
        document.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        table.addHeaderCell("Party").addHeaderCell("Details");
        table.addCell("Seller (Farmer A)").addCell(seller.getName() + "\n" + seller.getEmail() + "\n" + (seller.getPhone() != null ? seller.getPhone() : "") + "\n" + (seller.getLocation() != null ? seller.getLocation() : ""));
        table.addCell("Buyer (Farmer B)").addCell(agreement.getBuyerName() + "\n" + buyer.getEmail() + "\n" + (buyer.getPhone() != null ? buyer.getPhone() : "") + "\n" + (buyer.getLocation() != null ? buyer.getLocation() : ""));
        document.add(table);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Item: " + itemTitle).setFontSize(12));
        document.add(new Paragraph(itemDetails).setFontSize(10));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Final Price: INR " + agreement.getFinalPrice()).setFontSize(12).setBold());
        document.add(new Paragraph("Due Date: " + (agreement.getDueDate() != null ? agreement.getDueDate().toString() : "N/A")).setFontSize(12));
        document.add(new Paragraph(" "));
        if (agreement.getTerms() != null && !agreement.getTerms().isBlank()) {
            document.add(new Paragraph("Terms: " + agreement.getTerms()).setFontSize(10));
        }
        document.add(new Paragraph(" "));
        // Demo/testing: simulate digital signature verification with a green tick.
        Text tick = new Text(" ✓").setFontColor(ColorConstants.GREEN).setBold();
        document.add(new Paragraph("Digital Signature: VERIFIED")
                .add(tick)
                .setFontSize(10)
                .setItalic());
        document.add(new Paragraph("(To be signed by both parties)").setFontSize(8));

        document.close();
        return baos.toByteArray();
    }

    public String savePdf(byte[] pdfBytes, String subDir, String filename) throws Exception {
        Path dir = Paths.get(uploadDir, subDir);
        Files.createDirectories(dir);
        Path file = dir.resolve(filename);
        Files.write(file, pdfBytes);
        return "/api/files/" + subDir + "/" + filename;
    }
}
