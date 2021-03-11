package pdf.pdfbody.generic;

import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import pdf.pdfbody.bean.TableData;
import pdf.pdfdocument.SubTotalEvent;
import pdf.pdfdocument.Totals;
import pdf.utils.PdfUtilities;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BodyPdfTableBody {

    public static void arrayBody(PdfPTable table, List<TableData> data, List<Map<String, Object>> datas, Totals totals) {
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> dataMap = datas.get(i);
            for (int y = 0; y < data.size(); y++) {
                TableData dataBean = data.get(y);
                Object value = dataBean.getValue(dataMap);
                PdfPCell cell = new PdfPCell(new Phrase(PdfUtilities.formatValue(value), new Font(Font.FontFamily.UNDEFINED, 6)));
                cell.setBorder(0);
                cell.setBorderWidthLeft(0.5f);
                cell.setHorizontalAlignment(dataBean.getAlignment());
                if (i == datas.size() - 1) {
                    cell.setBorderWidthBottom(0.5f);
                }
                if (y == data.size() - 1) {
                    cell.setBorderWidthRight(0.5f);
                    BigDecimal total;
                    if (value instanceof BigDecimal) {
                        total = (BigDecimal) value;
                    } else if (value instanceof String) {
                        total = new BigDecimal((String) value);
                    } else if (value instanceof Integer) {
                        total = new BigDecimal((Integer) value);
                    } else {
                        throw new ClassCastException("Not possible to coerce ["+value+"] from class "+value.getClass()+" into a BigDecimal.");
                    }
                    cell.setCellEvent(new SubTotalEvent(totals, total));
                }
                table.addCell(cell);
            }
        }
    }
}
