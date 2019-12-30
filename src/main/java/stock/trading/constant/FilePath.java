package stock.trading.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePath {
    nseTargetStocksFile("/home/dpc/projects/stockTradingEstimator/src/main/java/stock/trading/data/NSE-Stocks.csv"),
    nseTargetSectorsFile("/home/dpc/projects/stockTradingEstimator/src/main/java/stock/trading/data/NSE-Sector.csv");

    private String value;
}
