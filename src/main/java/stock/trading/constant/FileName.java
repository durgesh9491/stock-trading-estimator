package stock.trading.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileName {
    nseTargetStocksFile("NSE-Stocks.csv"),
    nseTargetSectorsFile("NSE-Sector.csv");

    private final String value;
}
