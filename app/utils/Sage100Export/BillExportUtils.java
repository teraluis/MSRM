package utils.Sage100Export;

import api.v1.models.*;
import core.models.AddressWithRole;
import core.models.Prestation;
import establishments.EstablishmentAddressRole;
import estateWithAddress.EstateWithAddress;
import missionclient.interventions.DoneIntervention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import users.User;
import utils.VariablesExport.PaymentTypeExport;
import utils.VariablesExport.PaymentTypeExportUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BillExportUtils {
    protected final static Logger logger = LoggerFactory.getLogger(BillExportUtils.class);


    private static final String multiExpertId = "0_Multi_DI";

    private String cleanString(String stringToClean) {
        return Normalizer.normalize(stringToClean, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9]", " ").toUpperCase();
    }

    private String addPrestations(String oldPrestations, String newPrestation) {
        if (oldPrestations.equals("")) {
            return newPrestation;
        } else {
            return oldPrestations + ";" + newPrestation;
        }
    }

    private String getDesignation(api.v1.models.FactureLigne billLine) {
        if (billLine.designation.isPresent()) {
            return billLine.designation.get();
        } else {
            switch (billLine.refadx.toUpperCase()) {
                case "ELECTRICITE":
                    return "ELEC";
                case "GAZ":
                    return "GAZ";
                case "AMIANTE_PRI":
                    return "DAPP";
                case "AMIANTE_TRA":
                    return "RAAT";
                case "ANALYSE":
                    return "ANALYSE";
                default:
                    String prestations = "";
                    if (billLine.refadx.contains("EL")) {
                        prestations = addPrestations(prestations, "ELEC");
                    }
                    if (billLine.refadx.contains("GA")) {
                        prestations = addPrestations(prestations, "GAZ");
                    }
                    if (billLine.refadx.contains("DA")) {
                        prestations = addPrestations(prestations, "DAPP");
                    }
                    if (billLine.refadx.contains("RA")) {
                        prestations = addPrestations(prestations, "RAAT");
                    }
                    return prestations;
            }
        }
    }

    private void generateArticle(String clientCode, String order, DecimalFormat df, SimpleDateFormat ddMMyy, String quantity, List<String> fileLines, Optional<DoneIntervention> intervention, String prestationName, String tauxTaxe1, String tauxTaxe2, String tauxTaxe3, String refadx, Optional<BigDecimal> value, String agence) {
        String finalValue = value.isPresent() ? df.format(value.get()) : "";
        fileLines.add("#CHLI");
        fileLines.add(order);
        fileLines.add(refadx);
        fileLines.add(prestationName);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("1");
        fileLines.add(finalValue);
        fileLines.add("0.00");
        fileLines.add(quantity);
        fileLines.add(quantity);
        fileLines.add("Unite");
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0.00");
        fileLines.add("0.00");
        fileLines.add("0.00");
        fileLines.add("0");
        fileLines.add(intervention.map(inter -> cleanString(inter.getPlanning().getExpert().lastname)).orElse(multiExpertId));
        fileLines.add(intervention.map(inter -> cleanString(inter.getPlanning().getExpert().firstname)).orElse(""));
        fileLines.add("");
        fileLines.add(cleanString(agence));
        fileLines.add("");
        fileLines.add("1");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add(tauxTaxe1);
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add(tauxTaxe2);
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add(tauxTaxe3);
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add(clientCode);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add(ddMMyy.format(intervention.map(i -> i.getPlanning().getStartTime()).orElse(new Date()))); // TODO intervention date when several interventions ? Default today
        fileLines.add("1");
        fileLines.add("");
        fileLines.add("3");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("0");
    }

    private void exportPayment(Date deadline, String billName, String orderName, Date paymentDate, Date crecDate, String paymentValue, String clientCode, SimpleDateFormat ddMMyy, String compteCode, List<String> fileLines, PaymentTypeExport typeExport) {
        fileLines.add("#CRGT");
        fileLines.add("0");
        fileLines.add(clientCode);
        fileLines.add(ddMMyy.format(paymentDate));
        fileLines.add(orderName);
        fileLines.add(billName);
        fileLines.add(paymentValue);
        fileLines.add("0");
        fileLines.add("0.000000");
        fileLines.add("0.00");
        fileLines.add(typeExport.number);
        fileLines.add("0");
        fileLines.add(typeExport.code);
        fileLines.add(typeExport.recovery);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0.00");
        fileLines.add("");
        fileLines.add(compteCode);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add(clientCode);
        fileLines.add(ddMMyy.format(deadline));

        fileLines.add("#CREC");
        fileLines.add(billName);
        fileLines.add(ddMMyy.format(crecDate));
        fileLines.add(paymentValue);
    }

    private void generateCHEN(final String billName, final String order, final Optional<DoneIntervention> intervention, final String clientCode, final SimpleDateFormat ddMMyy, final String compteCode, final String agence, final List<String> fileLines, final Date exportDate, final Boolean isCreditNote) {

        String billOrCreditNote = isCreditNote ? "3" : "1";
        fileLines.add("#CHEN");
        fileLines.add("1");
        fileLines.add("7");
        fileLines.add(billOrCreditNote);
        fileLines.add("1");
        fileLines.add(billName);
        fileLines.add(ddMMyy.format(exportDate));
        fileLines.add(order);
        fileLines.add(ddMMyy.format(intervention.map(i -> i.getPlanning().getStartTime()).orElse(new Date()))); // TODO intervention date when several interventions ? Default today
        fileLines.add("");
        fileLines.add("");
        fileLines.add(clientCode);
        fileLines.add(cleanString(agence));
        fileLines.add(order);
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add("0.000000");
        fileLines.add(clientCode);
        fileLines.add("1");
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add(intervention.map(inter -> cleanString(inter.getPlanning().getExpert().lastname)).orElse(multiExpertId));
        fileLines.add(intervention.map(inter -> cleanString(inter.getPlanning().getExpert().firstname)).orElse(""));
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("1");
        fileLines.add("21");
        fileLines.add("11");
        fileLines.add("1");
        fileLines.add("1");
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add("2");
        fileLines.add(compteCode);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0.00");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("0.00");
        fileLines.add("0");
        fileLines.add("0.0000");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("0.0000");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("0.0000");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
    }

    private void generateCHLI(String clientCode, String order, DecimalFormat df, SimpleDateFormat ddMMyy, String quantity, List<String> fileLines, Optional<DoneIntervention> intervention, Optional<FactureLigne> billLine, String agence, String billName) {
        String tauxTaxe1 = "0.0000";
        String tauxTaxe2 = "0.0000";
        String tauxTaxe3 = "0.0000";
        BigDecimal tauxRate = BigDecimal.ONE;
        if (!billLine.isPresent()) {
            String prestations = cleanString("Avoir sur facture " + billName);
            generateArticle(clientCode, order, df, ddMMyy, quantity, fileLines, intervention, prestations, tauxTaxe1, tauxTaxe2, tauxTaxe3, "", Optional.empty(), agence);
        } else {
            // Taxe à 20%
            if (billLine.get().tvacode.equals("20%")) {
                tauxTaxe3 = "20.0000";
                tauxRate = new BigDecimal("1.20");
            }
            String designation = getDesignation(billLine.get());
            String refAdx = billLine.get().refbpu.orElseGet(billLine.get().refadx::toUpperCase);
            BigDecimal value = billLine.get().price.multiply(tauxRate);
            generateArticle(clientCode, order, df, ddMMyy, quantity, fileLines, intervention, designation, tauxTaxe1, tauxTaxe2, tauxTaxe3, refAdx, Optional.of(value), agence);
            if (billLine.get().discount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remiseHT = (billLine.get().discount.multiply(BigDecimal.valueOf(-1)).multiply(billLine.get().price)).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                BigDecimal remiseTTC = quantity.equals("-1.00") ? BigDecimal.ZERO : remiseHT.multiply(tauxRate);
                generateArticle(clientCode, order, df, ddMMyy, quantity, fileLines, intervention, "REMISE DE " + billLine.get().discount.toString() + "% SUR " + designation, tauxTaxe1, tauxTaxe2, tauxTaxe3, "REMISEVTE", Optional.of(remiseTTC), agence);
            }
        }

    }

    private void generateCCLI(String clientName, String commercialName, String clientCode, String
            clientAddress1, String clientAddress2, String clientPostCode, String clientCity, String clientType, String
                                      mobilePhone, String siret, String created, String compteCode, List<String> fileLines) {
        fileLines.add("#CCLI");
        fileLines.add(clientCode);
        fileLines.add(compteCode);
        fileLines.add("");
        fileLines.add(cleanString(clientName));
        fileLines.add("");
        fileLines.add("");
        fileLines.add(cleanString(clientAddress1));
        fileLines.add(cleanString(clientAddress2));
        fileLines.add(cleanString(clientPostCode));
        fileLines.add(cleanString(clientCity));
        fileLines.add("");
        fileLines.add("");
        fileLines.add(clientType);
        fileLines.add(cleanString(commercialName));
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("");
        fileLines.add(clientCode);
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add("1");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("1");
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("1");
        fileLines.add("0");
        fileLines.add(siret);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add(created);
        fileLines.add(mobilePhone);
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0.0000");
        fileLines.add("0.0000");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("");
        fileLines.add("0");
        fileLines.add("1");
        fileLines.add(compteCode);
    }

    private void generateCIVA(String clientCode, Order order, String clientName, String clientPostCode, String
            clientCity, Optional<String> referenceNumber, Optional<DoneIntervention> intervention, SimpleDateFormat
                                      formatter, List<String> fileLines) {
        fileLines.add("#CIVA");
        fileLines.add("");
        fileLines.add(clientCode);
        fileLines.add("DATE DE NOTRE INTERVENTION " + formatter.format(intervention.map(i -> i.getPlanning().getStartTime()).orElse(new Date())));
        fileLines.add("A LA DEMANDE DE " + cleanString(clientName + " " + clientPostCode + " " + clientCity));
        fileLines.add(clientName);
        fileLines.add(referenceNumber.map(ref -> "VOS REFERENCES " + ref).orElse(""));
        fileLines.add("FACTURE EDITEE PAR ... "); // définir l'utilisateur
        fileLines.add("SUPERFICIE A DETERMINER"); // Superficie à déterminer
        fileLines.add("N DE MISSION " + order.name); // code mission
        fileLines.add("COURRIER");
        fileLines.add("");
        fileLines.add("");
        fileLines.add("0.0000");
    }

    public BillExportUtils() {
    }

    public List<String> getExportLines(final Facture bill, final Order order, final Account account,
                                       final Entity entity, final FullEstablishment establishment, final User commercial,
                                       final List<DoneIntervention> interventions, final Date exportDate, final List<EstateWithAddress> estates,
                                       final Optional<String> referenceNumber) {
        DecimalFormat df = new DecimalFormat("#0.00");
        SimpleDateFormat ddMMyy = new SimpleDateFormat("ddMMyy");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        final String commercialName = commercial.first_name + " " + commercial.last_name;
        final Optional<DoneIntervention> intervention = interventions.size() > 1 ? Optional.empty() : Optional.of(interventions.get(0));

        //Freeze tva code
        final String compteCode = "411001";
        final String clientName = entity.name;
        final String clientCode = account.legacyCode.orElse("");
        Optional<AddressWithRole> firstAddress = establishment.addresses.stream().anyMatch(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString()))
                ? establishment.addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString())).findAny()
                : establishment.addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.MAIN.toString())).findAny();
        // Must have an address
        final String clientAddress1 = firstAddress.get().address.address1.orElse("");
        final String clientAddress2 = firstAddress.get().address.address2.orElse("");
        final String clientPostCode = firstAddress.get().address.postCode.orElse("");
        final String clientCity = firstAddress.get().address.city.orElse("");
        final String mobilePhone = establishment.establishment.phone.orElse("");
        final String siret = establishment.establishment.siret;
        final String created = ddMMyy.format(establishment.establishment.created);
        final String clientType = "GCL"; // TODO determine client type

        //Freeze agence
        final String agence = "AGENCE CALVADOS";

        List<String> fileLines = new ArrayList<>();

        // Address vars from estate
        final Prestation anyPrestation = interventions.stream().findAny().get().getPrestations().stream().findAny().get();
        final Optional<addresses.Address> addressOfFirstLocality;
        Optional<EstateWithAddress> estate = anyPrestation.targetId.isPresent() ?
                estates.stream().filter(e -> e.localities.stream().anyMatch(l -> l.annexes.stream().anyMatch(a -> a.id.equals(anyPrestation.targetId.get())) || l.premises.stream().anyMatch(p -> p.id.equals(anyPrestation.targetId.get())))).findFirst()
                : estates.stream().filter(e -> e.localities.size() > 0 && e.localities.get(0).addresses.size() > 0).findFirst();
        if (estate.isPresent()) {
            addressOfFirstLocality = Optional.of(estate.get().localities.get(0).addresses.get(0));
        } else {
            addressOfFirstLocality = Optional.empty();
        }
        final Optional<String> address1 = addressOfFirstLocality.flatMap(address -> address.address1);
        final Optional<String> address2 = addressOfFirstLocality.flatMap(address -> address.address2);

        final Optional<String> postCode = addressOfFirstLocality.flatMap(address -> address.postCode);
        final Optional<String> city = addressOfFirstLocality.flatMap(address -> address.city);

        if (!bill.exportDate.isPresent()) {
            generateCCLI(clientName, commercialName, clientCode, clientAddress1, clientAddress2, clientPostCode, clientCity, clientType, mobilePhone, siret, created, compteCode, fileLines);

            fileLines.add("#CCDL");
            fileLines.add(order.name);
            fileLines.add(cleanString(address1.orElse("")));
            fileLines.add(cleanString(address2.orElse("")));
            fileLines.add(cleanString(postCode.orElse("")));
            fileLines.add(cleanString(city.orElse("")));
            fileLines.add("");
            fileLines.add("");
            fileLines.add("");
            fileLines.add("1");
            fileLines.add("1");
            fileLines.add("1");
            fileLines.add("00 00 00 00 00");
            fileLines.add("");
            fileLines.add("");

            generateCHEN(bill.name, order.name, intervention, clientCode, ddMMyy, compteCode, agence, fileLines, exportDate, false);

            generateCIVA(clientCode, order, clientName, clientPostCode, clientCity, referenceNumber, intervention, formatter, fileLines);

            for (api.v1.models.FactureLigne factureLigne : bill.lignes) {
                generateCHLI(clientCode, order.name, df, ddMMyy, df.format(factureLigne.quantity), fileLines, intervention, Optional.of(factureLigne), agence, bill.name);
            }

            fileLines.add("#CHRE");
            fileLines.add("2");
            fileLines.add(ddMMyy.format(bill.deadline));
            fileLines.add("");
            fileLines.add("0.00");
            fileLines.add("0.00");
            fileLines.add("1");
            fileLines.add("0");
            fileLines.add("");
        }

        for (Avoir creditNote : bill.creditnotes) {
            if (!creditNote.exportdate.isPresent()) {
                generateCCLI(clientName, commercialName, clientCode, clientAddress1, clientAddress2, clientPostCode, clientCity, clientType, mobilePhone, siret, created, compteCode, fileLines);
                generateCHEN(creditNote.name, order.name, intervention, clientCode, ddMMyy, compteCode, agence, fileLines, exportDate, true);
                generateCIVA(clientCode, order, clientName, clientPostCode, clientCity, referenceNumber, intervention, formatter, fileLines);
                generateCHLI(clientCode, order.name, df, ddMMyy, "-1.00", fileLines, intervention, Optional.empty(), agence, bill.name);
                for (api.v1.models.FactureLigne ligne : creditNote.lignes) {
                    generateCHLI(clientCode, order.name, df, ddMMyy, df.format(-1 * ligne.quantity), fileLines, intervention, Optional.of(ligne), agence, bill.name);
                }
            }
        }

        final Boolean hasCreditNote = bill.creditnotes.size() > 0;
        PaymentTypeExport creditNoteTypeExport = new PaymentTypeExport("25", "AVA", "511500");
        for (Paiement payment : bill.paiements) {
            if (!payment.exportDate.isPresent()) {
                if (!hasCreditNote) {
                    PaymentTypeExport paymentTypeExport = PaymentTypeExportUtils.paymentTypeMap().get(payment.type);
                    exportPayment(bill.deadline, bill.name, order.name, payment.date, bill.deadline, df.format(payment.value), clientCode, ddMMyy, compteCode, fileLines, paymentTypeExport);
                } else {
                    exportPayment(bill.deadline, bill.name, order.name, payment.date, bill.deadline, df.format(payment.value), clientCode, ddMMyy, compteCode, fileLines, creditNoteTypeExport);
                }
            }
            if (hasCreditNote && !bill.exportDate.isPresent()) {
                Avoir creditNote = bill.creditnotes.get(0);
                exportPayment(bill.deadline, creditNote.name, order.name, creditNote.date, creditNote.date, "-" + df.format(payment.value), clientCode, ddMMyy, compteCode, fileLines, creditNoteTypeExport);
            }
        }
        return fileLines;
    }

}
