package pdf.pdfbody.bean;

import com.itextpdf.text.Element;

import java.util.Map;

public class TableData {

    private String key;
    private int alignment;

    public TableData(String key, int alignment) {
        this.key = key;
        this.alignment = alignment;
    }

    public TableData(String key) {
        this(key, Element.ALIGN_LEFT);
    }

    public String getKey() {
        return key;
    }

    public Object getValue(Map<String, Object> datas){
        return datas.getOrDefault(this.key, null);
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
}
