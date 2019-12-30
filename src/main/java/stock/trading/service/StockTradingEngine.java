package stock.trading.service;

import stock.trading.bean.SectorDetail;
import stock.trading.bean.StockDetail;
import stock.trading.validation.DataValidator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class StockTradingEngine {
    @Inject
    private TradingEstimateService estimateService;

    public void run(double totalCapital) {
        try {
            Set<StockDetail> allStockDetails = estimateService.getTargetStockDetails();
            Set<SectorDetail> allSectorDetails = estimateService.getTargetSectorDetails();

            Set<SectorDetail> targetSectorDetails = estimateService.getApplicableSectorDetail(allSectorDetails);
            Set<StockDetail> targetStockDetails = estimateService.getApplicableStockDetails(targetSectorDetails, allStockDetails);

            DataValidator.validateStockAllocation(estimateService.getSectorWiseStockAllocationMap(targetStockDetails));
            DataValidator.validateSectorAllocation(targetSectorDetails.parallelStream().map(SectorDetail::getWeight).collect(Collectors.toList()));

            estimateService.fillLatestStockPrice(targetStockDetails);

            double prevCapital = -1, currentCapital = totalCapital;
            int counter = 0;
            while (currentCapital > 0 && prevCapital != currentCapital) {
                System.out.println("Estimation iteration - :" + (++counter));
                Map<Integer, Set<StockDetail>> sectorToStocksMap = estimateService.allocateStocks(targetSectorDetails, targetStockDetails, currentCapital);
                double totalTradingAmount = estimateService.calculateTradingAmount(allSectorDetails, sectorToStocksMap);
                double amountLeft = Math.floor(totalCapital - totalTradingAmount);

                prevCapital = currentCapital;
                currentCapital = amountLeft;
                System.out.println(String.format("\n\nAmount left after trading adjustment : %s", amountLeft));
            }
        } catch (IOException ex) {
            System.out.println(String.format("Expected error caught! : %s", ex));
        } catch (Exception ex) {
            System.out.println(String.format("Something went wrong, which server couldn't understand! : %s", ex));
            ex.printStackTrace();
        }
    }
}
