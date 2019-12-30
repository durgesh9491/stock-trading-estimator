package stock.trading.service;

import stock.trading.validation.DataValidator;
import stock.trading.bean.SectorDetail;
import stock.trading.bean.StockDetail;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StockTradingEngine {
    private TradingEstimateService estimateService = new TradingEstimateService();

    public void run(double totalCapital) {
        Set<StockDetail> targetStockDetails = estimateService.getApplicableStockDetails(estimateService.getTargetStockDetails());
        Set<SectorDetail> targetSectorDetails = estimateService.getApplicableSectorDetail(estimateService.getTargetSectorDetails());

        DataValidator.validateStockAllocation(estimateService.getSectorWiseStockAllocationMap(targetStockDetails));
        DataValidator.validateSectorAllocation(targetSectorDetails.parallelStream().map(SectorDetail::getWeight).collect(Collectors.toList()));

        estimateService.fillLatestStockPrice(targetStockDetails);
        Map<Integer, Set<StockDetail>> sectorToStocksMap = estimateService.allocateStocks(targetSectorDetails, targetStockDetails, totalCapital);
        double totalTradingAmount = estimateService.calculateTradingAmount(sectorToStocksMap);

        System.out.println(String.format("Amount left after trading adjustment : %s", Math.floor(totalCapital - totalTradingAmount)));
    }
}
