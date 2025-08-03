package me.skorb.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import me.skorb.entity.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class InvoiceUtils {

    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font REGULAR_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL);

    private static final String N_A = "N/A";

    public static File generateInvoice(Order order) {
        String fileName = "Invoice_" + order.getId() + ".pdf";
        File file = new File(fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();

            // Add Invoice Title
            document.add(new Paragraph("Invoice for Order #" + order.getId(), HEADER_FONT));
            document.add(new Paragraph("\n")); // Space

            // Create a parent table with 2 columns for layout
            PdfPTable layoutTable = new PdfPTable(2);
            layoutTable.setWidthPercentage(100); // Full page width
            layoutTable.setSpacingBefore(10f);
            layoutTable.setWidths(new float[]{1, 1}); // 50-50 split for two columns

            // Generate customer and vehicle tables
            PdfPTable customerTable = generateCustomerTable(order.getCustomer());
            PdfPTable vehicleTable = generateVehicleTable(order.getVehicle());

            // Add both tables to the layout table
            layoutTable.addCell(new PdfPCell(customerTable)); // Left Column: Customer Info
            layoutTable.addCell(new PdfPCell(vehicleTable)); // Right Column: Vehicle Info

            // Add layout table to document
            document.add(layoutTable);

            // Space before services and parts sections
            document.add(new Paragraph("\n"));

            // Add Services
            document.add(new Paragraph("Services Provided:\n", HEADER_FONT));
            PdfPTable servicesTable = generateServicesTable(order);
            document.add(servicesTable);

            document.add(new Paragraph("\n"));

            // Add Parts
            document.add(new Paragraph("Parts Used:\n", HEADER_FONT));
            PdfPTable partsTable = generatePartsTable(order);
            document.add(partsTable);

            String totalInvoice = String.format(Locale.US, "\nTotal Invoice: $ %.2f", order.getTotalCost());
            document.add(new Paragraph(totalInvoice, HEADER_FONT));

            document.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }

        return file;  // Return the generated PDF file
    }

    private static PdfPTable generateCustomerTable(Customer customer) {
        PdfPTable customerTable = new PdfPTable(2);
        customerTable.setWidthPercentage(100);

        customerTable.addCell(getBoldCell("CUSTOMER", 2));

        String firstName = customer.getFirstName() != null ? customer.getFirstName() : N_A;
        String lastName = customer.getLastName() != null ? customer.getLastName() : N_A;
        String fullName = firstName + " " + lastName;
        addTableRow(customerTable, "Name:", fullName);

        String address = customer.getAddress() != null ? customer.getAddress() : N_A;
        addTableRow(customerTable, "Address:", address);

        String city = customer.getCity() != null ? customer.getCity() : N_A;
        addTableRow(customerTable, "City:", city);

        String province = customer.getState() != null ? customer.getState().name() : N_A;
        addTableRow(customerTable, "Province:", province);

        System.out.println(province);

        String postalCode = customer.getPostalCode() != null ? customer.getPostalCode() : N_A;
        addTableRow(customerTable, "Postal Code:", postalCode);

        String phone = customer.getPhone() != null ? customer.getPhone() : N_A;
        addTableRow(customerTable, "Phone:", phone);

        String email = customer.getEmail() != null ? customer.getEmail() : N_A;
        addTableRow(customerTable, "Email:", email);

        return customerTable;
    }

    private static PdfPTable generateVehicleTable(Vehicle vehicle) {
        PdfPTable vehicleTable = new PdfPTable(2);
        vehicleTable.setWidthPercentage(100);

        vehicleTable.addCell(getBoldCell("VEHICLE", 2));

        String make = vehicle.getMake() != null ? vehicle.getMake().getName() : N_A;
        addTableRow(vehicleTable, "Make:", make);

        String model = vehicle.getModel() != null ? vehicle.getModel().getName() : N_A;
        addTableRow(vehicleTable, "Model:", model);

        String year = vehicle.getYear() != null ? vehicle.getYear().toString() : N_A;
        addTableRow(vehicleTable, "Year:", year);

        String vin = vehicle.getVin() != null ? vehicle.getVin() : N_A;
        addTableRow(vehicleTable, "VIN:", vin);

        String licensePlate = vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : N_A;
        addTableRow(vehicleTable, "License Plate:", licensePlate);

        return vehicleTable;
    }

    private static PdfPTable generateServicesTable(Order order) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell(getBoldCell("Service", 1));
        table.addCell(getBoldCell("Price ($)", 1));

        for (Service service : order.getServicesProvided()) {
            addTableRow(table, service.getName(), String.valueOf(service.getPrice()));
        }

        return table;
    }

    private static PdfPTable generatePartsTable(Order order) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        table.addCell(getBoldCell("Part", 1));
        table.addCell(getBoldCell("Quantity", 1));
        table.addCell(getBoldCell("Price ($)", 1));

        for (Part part : order.getPartsWithQuantities().keySet()) {
            int quantity = order.getPartsWithQuantities().get(part);
            addTableRow(table, part.getName(), String.valueOf(quantity), String.valueOf(part.getPrice()));
        }

        return table;
    }

    private static void addTableRow(PdfPTable table, String col1, String col2) {
        table.addCell(new PdfPCell(new Phrase(col1, REGULAR_FONT)));
        table.addCell(new PdfPCell(new Phrase(col2, REGULAR_FONT)));
    }

    private static void addTableRow(PdfPTable table, String col1, String col2, String col3) {
        table.addCell(new PdfPCell(new Phrase(col1, REGULAR_FONT)));
        table.addCell(new PdfPCell(new Phrase(col2, REGULAR_FONT)));
        table.addCell(new PdfPCell(new Phrase(col3, REGULAR_FONT)));
    }

    private static PdfPCell getBoldCell(String text, int colspan) {
        Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, boldFont));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        return cell;
    }
}
