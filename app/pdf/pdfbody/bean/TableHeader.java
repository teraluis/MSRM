package pdf.pdfbody.bean;

public class TableHeader {

    private String name;

    private int percentWith;

    private boolean hasTotal;

    public TableHeader(String name, int percentWidth) {
        this.name = name;
        this.percentWith = percentWidth;
        this.hasTotal = false;
    }

    public TableHeader(String name, int percentWidth, boolean hasTotal) {
        this(name, percentWidth);
        this.hasTotal = hasTotal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPercentWith() {
        return percentWith;
    }

    public Integer getNbColumn(){
        return 1;
    }

    public boolean hasTotal() {
        return hasTotal;
    }
}
