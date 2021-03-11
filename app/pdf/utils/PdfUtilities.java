package pdf.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import core.models.Address;
import core.models.AddressWithRole;
import establishments.EstablishmentAddressRole;
import io.netty.util.internal.StringUtil;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;

public class PdfUtilities {
    public static final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * Methode transformant un code couleur hexa (#...) en eun entier utilisable par ipdf
     *
     * @param hexCode : le code hexa de la couleur
     * @return BaseColor correspondant à ce code hexa
     */
    public static BaseColor computeRGBColorFromHexCode(String hexCode) {
        Color color = Color.decode(hexCode);
        return new BaseColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void insertValueFromJson(String key, ObjectNode json, Map<String, Object> destMap) {

        String value = json.has(key) ? json.get(key).textValue() : null;
        if (StringUtil.isNullOrEmpty(value) || "null".equals(value)) {
            value = "";
        }
        destMap.put(key, value);
    }

    public static Long tryParseLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static PdfPCell invisibleCell() {
        PdfPCell c = new PdfPCell();
        c.setBorder(0);
        return c;
    }

    public static PdfPCell createImageCell(String path) throws DocumentException, IOException {
        com.itextpdf.text.Image img = Image.getInstance(path);
        PdfPCell cell = new PdfPCell(img, true);
        cell.setBorder(0);
        return cell;
    }

    public static void addCell(PdfPTable main, PdfPTable sub) {
        PdfPCell cell = new PdfPCell(sub);
        cell.setBorder(0);
        main.addCell(cell);
    }

    public static void addCell(PdfPTable main, PdfPCell sub) {
        PdfPCell cell = new PdfPCell(sub);
        cell.setBorder(0);
        main.addCell(cell);
    }

    public static void addCell(PdfPTable main, Phrase sub) {
        PdfPCell cell = new PdfPCell(sub);
        cell.setBorder(0);
        main.addCell(cell);
    }

    public static String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(date);
    }

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public static String formatValue(Object value) {
        if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
            return df.format(value);
        }

        return value.toString();
    }

    public static String getBillStreetAddress(java.util.List<AddressWithRole> addresses) {
        Optional<AddressWithRole> billingAddress = addresses
                .stream()
                .filter(a -> a.role.equals(EstablishmentAddressRole.BILLING.toString()))
                .findFirst();
        if (billingAddress.isPresent()) {
            return billingAddress.get().address.address1.orElse("") + " " + billingAddress.get().address.address2.map(a -> "\n" + a).orElse("");
        } else {
            Optional<AddressWithRole> mainAddress = addresses
                    .stream()
                    .filter(a -> a.role.equals(EstablishmentAddressRole.MAIN.toString()))
                    .findFirst();
            Address address = mainAddress.orElseThrow(() -> new AssertionError("No address")).address;
            return address.address1.orElse("") + " " + address.address2.map(a -> "\n" + a).orElse("");
        }
    }

    public static String getBillCity(java.util.List<AddressWithRole> addresses) {
        Optional<AddressWithRole> billingAddress = addresses
                .stream()
                .filter(a -> a.role.equals(EstablishmentAddressRole.BILLING.toString()))
                .findFirst();
        if (billingAddress.isPresent()) {
            return billingAddress.get().address.postCode.orElse("") + " " + billingAddress.get().address.city.orElse("");
        } else {
            Optional<AddressWithRole> mainAddress = addresses
                    .stream()
                    .filter(a -> a.role.equals(EstablishmentAddressRole.MAIN.toString()))
                    .findFirst();
            Address address = mainAddress.orElseThrow(() -> new AssertionError("No address")).address;
            return address.postCode.orElse("") + " " + address.city.orElse("");
        }
    }

    public static String getBillCountry(List<AddressWithRole> addresses) {
        Optional<AddressWithRole> billingAddress = addresses
                .stream()
                .filter(a -> a.role.equals(EstablishmentAddressRole.BILLING.toString()))
                .findFirst();
        if (billingAddress.isPresent()) {
            return billingAddress.get().address.country.orElse("");
        } else {
            Optional<AddressWithRole> mainAddress = addresses
                    .stream()
                    .filter(a -> a.role.equals(EstablishmentAddressRole.MAIN.toString()))
                    .findFirst();
            Address address = mainAddress.orElseThrow(() -> new AssertionError("No address")).address;
            return address.country.orElseThrow(() -> new NullPointerException("No country"));
        }
    }

    public static PdfPCell setTextToRight(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, DEFAULT_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
