package stock.trading.bean;

import lombok.Data;

@Data
public class SectorDetail {
    private int id;
    private String name;
    private boolean applicable = false;
    private Double weight;
}
