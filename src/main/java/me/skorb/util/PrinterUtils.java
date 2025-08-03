package me.skorb.util;

import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.TextArea;
import me.skorb.entity.Order;
import me.skorb.entity.Part;
import me.skorb.entity.Service;
import me.skorb.view.ViewUtils;

import java.math.BigDecimal;
import java.util.Map;

public class PrinterUtils {

    public static String generateInvoiceText(Order order) {
        StringBuilder invoice = new StringBuilder();
        invoice.append("          CAR SERVICE INVOICE\n");
        invoice.append("=====================================\n");
        invoice.append("Order ID: ").append(order.getId()).append("\n");
        invoice.append("Date: ").append(order.getDate()).append("\n");
        invoice.append("Customer: ").append(order.getCustomer().getFirstName()).append(" ")
                .append(order.getCustomer().getLastName()).append("\n");
        invoice.append("-------------------------------------\n");

        invoice.append("Services Provided:\n");
        BigDecimal totalServiceCost = BigDecimal.ZERO;
        for (Service service : order.getServicesProvided()) {
            invoice.append(" - ").append(service.getName())
                    .append(" - $").append(service.getPrice()).append("\n");
            totalServiceCost = totalServiceCost.add(service.getPrice());
        }

        invoice.append("\nParts Used:\n");
        BigDecimal totalPartsCost = BigDecimal.ZERO;
        for (Map.Entry<Part, Integer> entry : order.getPartsWithQuantities().entrySet()) {
            Part part = entry.getKey();
            int quantity = entry.getValue();
            BigDecimal partTotal = part.getPrice().multiply(BigDecimal.valueOf(quantity));

            invoice.append(" - ").append(part.getName())
                    .append(" (Qty: ").append(quantity)
                    .append(") - $").append(partTotal).append("\n");

            totalPartsCost = totalPartsCost.add(partTotal);
        }

        invoice.append("=====================================\n");
        BigDecimal totalCost = totalServiceCost.add(totalPartsCost);
        invoice.append("TOTAL: $").append(totalCost).append("\n");

        return invoice.toString();
    }

    public static void printInvoice(String invoiceText) {
        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) {
            ViewUtils.showAlert("No Printer Found", "Please connect a printer before printing.");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            ViewUtils.showAlert("Print Error", "Failed to create a print job.");
            return;
        }

        TextArea invoiceArea = new TextArea(invoiceText);
        invoiceArea.setWrapText(true);
        invoiceArea.setPrefSize(400, 600);

        if (job.showPrintDialog(null)) {
            boolean success = job.printPage(invoiceArea);
            if (success) {
                job.endJob();
                ViewUtils.showAlert("Print Successful", "Invoice printed successfully.");
            } else {
                ViewUtils.showAlert("Print Failed", "Could not print the invoice.");
            }
        }
    }

//    public static void printInvoice(File pdfFile) {
//        if (pdfFile.exists()) {
//            Stage printStage = new Stage();  // Temporary JavaFX stage for printing
//            WebView webView = new WebView();
//            WebEngine webEngine = webView.getEngine();
//
//            // Load PDF in WebView (Requires PDF viewer support in the OS)
//            webEngine.load(pdfFile.toURI().toString());
//
//            // Create a scene and attach it to the stage
//            Scene scene = new Scene(webView);
//            printStage.setScene(scene);
//            printStage.show(); // Show it for rendering before printing
//
//            PrinterJob job = PrinterJob.createPrinterJob();
//            if (job != null) {
//                boolean success = job.printPage(webView);
//                if (success) {
//                    job.endJob();
//                }
//            }
//
//            printStage.close(); // Close the stage after printing
//        } else {
//            System.out.println("Invoice file not found: " + pdfFile.getAbsolutePath());
//        }
//    }

}
