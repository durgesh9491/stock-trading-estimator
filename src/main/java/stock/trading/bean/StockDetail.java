package stock.trading.bean;

import lombok.Data;

@Data
public class StockDetail {
    private String displayName;
    private String queryName;
    private double price;
    private int sectorId;
    private boolean applicable = false;
    private Double weight;
    private int units = 0;
}
