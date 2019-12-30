package stock.trading.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import stock.trading.bean.SectorDetail;
import stock.trading.bean.StockDetail;
import stock.trading.client.NseClient;
import stock.trading.constant.FilePath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

class TradingEstimateService {

    private NseClient nseClient = new NseClient();

    private BufferedReader reader;

    double calculateTradingAmount(Map<Integer, Set<StockDetail>> sectorToStocksMap) {
        double totalAmount = sectorToStocksMap.entrySet().stream().map(entry -> {
            double sectorWiseAmount = entry.getValue().stream().map(stock -> stock.getPrice() * stock.getUnits()).mapToDouble(Double::doubleValue).sum();
            System.out.println(String.format("SectorId : %s target trading amount : %.2f", entry.getKey(), sectorWiseAmount));
            return sectorWiseAmount;
        }).mapToDouble(Double::doubleValue).sum();

        System.out.println(String.format("Total trading amount : %.2f", totalAmount));
        return totalAmount;
    }

    Map<Integer, Set<StockDetail>> allocateStocks(Set<SectorDetail> targetSectorDetails, Set<StockDetail> targetStockDetails, double totalCapital) {
        Map<Integer, SectorDetail> idToSectorDetailMap = getSectorIdToObjMap(targetSectorDetails);
        Map<Integer, Set<StockDetail>> sectorIdToStocksMap = getSectorIdToStocksMap(targetStockDetails);

        sectorIdToStocksMap.forEach((sectorId, stocks) -> {
            SectorDetail sectorDetail = Optional.
                    ofNullable(idToSectorDetailMap.get(sectorId)).
                    orElseThrow(() -> new RuntimeException(String.format("SectorId : %d is not applicable!", sectorId)));

            System.out.println();
            double sectorWeight = sectorDetail.getWeight();
            stocks.forEach(stock -> {
                double stockWeight = stock.getWeight();
                int units = (int) ((totalCapital * sectorWeight * stockWeight) / stock.getPrice());
                stock.setUnits(units);
                System.out.println(String.format("Sector : %s | Stock : %s | Price : %s | Units : %s", sectorDetail.getName(), stock.getDisplayName(), stock.getPrice(), units));
            });
        });
        return sectorIdToStocksMap;
    }

    Set<SectorDetail> getTargetSectorDetails() {
        Set<SectorDetail> sectorDetails = Sets.newHashSet();
        String targetSector = "";
        try (FileReader fileReader = new FileReader(FilePath.nseTargetSectorsFile.getValue())) {
            reader = new BufferedReader(fileReader);
            String line = "";
            String cvsSplitBy = ",";
            int lineCounter = 0;
            while (Objects.nonNull(line = reader.readLine())) {
                if (++lineCounter == 1) {
                    continue;
                }
                String[] sectorInfo = line.split(cvsSplitBy);
                targetSector = sectorInfo[1];
                sectorDetails.add(buildSectorDetailObj(sectorInfo));
            }
        } catch (Exception ex) {
            System.out.println("Unable to complete the process for sector : " + targetSector);
            ex.printStackTrace();
        }
        return sectorDetails;
    }

    Set<StockDetail> getTargetStockDetails() {
        Set<StockDetail> stockDetails = Sets.newHashSet();
        String targetStock = "";
        try (FileReader fileReader = new FileReader(FilePath.nseTargetStocksFile.getValue())) {
            reader = new BufferedReader(fileReader);
            String line = "";
            String cvsSplitBy = ",";
            int lineCounter = 0;
            while (Objects.nonNull(line = reader.readLine())) {
                if (++lineCounter == 1) {
                    continue;
                }
                stockDetails.add(buildStockDetailObj(line.split(cvsSplitBy)));
            }
        } catch (Exception ex) {
            System.out.println("Unable to complete the process for stock : " + targetStock);
            ex.printStackTrace();
        }
        return stockDetails;
    }

    Set<StockDetail> getApplicableStockDetails(Set<StockDetail> stockDetails) {
        return stockDetails.parallelStream().filter(StockDetail::isApplicable).collect(Collectors.toSet());
    }

    Set<SectorDetail> getApplicableSectorDetail(Set<SectorDetail> sectorDetails) {
        return sectorDetails.parallelStream().filter(SectorDetail::isApplicable).collect(Collectors.toSet());
    }

    Map<Integer, List<Double>> getSectorWiseStockAllocationMap(Set<StockDetail> stockDetails) {
        Map<Integer, List<Double>> sectorWiseStockAllocationMap = Maps.newHashMap();
        stockDetails.forEach(stockDetail -> {
            sectorWiseStockAllocationMap.putIfAbsent(stockDetail.getSectorId(), Lists.newArrayList());
            sectorWiseStockAllocationMap.get(stockDetail.getSectorId()).add(stockDetail.getWeight());
        });
        return sectorWiseStockAllocationMap;
    }

    void fillLatestStockPrice(Set<StockDetail> targetStockDetails) {
        long startTime = System.currentTimeMillis();
        targetStockDetails.parallelStream().forEach(stockDetail -> {
            stockDetail.setPrice(nseClient.getLatestStockPrice(stockDetail.getQueryName()));
        });
        System.out.println(String.format("Time taken to get latest stock prices : %s seconds", (System.currentTimeMillis() - startTime) / TimeUnit.SECONDS.toMillis(1)));
    }

    private StockDetail buildStockDetailObj(String[] stockInfo) {
        StockDetail stockDetail = new StockDetail();
        stockDetail.setDisplayName(stockInfo[0]);
        stockDetail.setQueryName(stockInfo[1]);
        stockDetail.setSectorId(Integer.parseInt(stockInfo[2]));
        int applicable = Integer.parseInt(stockInfo[3]);
        if (applicable > 0) {
            stockDetail.setApplicable(true);
        }
        if (stockDetail.isApplicable()) {
            stockDetail.setWeight(Double.parseDouble(stockInfo[4]) / 100);
        }
        return stockDetail;
    }

    private SectorDetail buildSectorDetailObj(String[] sectorInfo) {
        SectorDetail sectorDetail = new SectorDetail();
        sectorDetail.setId(Integer.parseInt(sectorInfo[0]));
        sectorDetail.setName(sectorInfo[1]);
        int applicable = Integer.parseInt(sectorInfo[2]);
        if (applicable > 0) {
            sectorDetail.setApplicable(true);
        }
        if (sectorDetail.isApplicable()) {
            sectorDetail.setWeight(Double.parseDouble(sectorInfo[3]) / 100);
        }
        return sectorDetail;
    }

    private Map<Integer, SectorDetail> getSectorIdToObjMap(Set<SectorDetail> targetSectorDetails) {
        return targetSectorDetails.stream().collect(Collectors.toMap(SectorDetail::getId, Function.identity()));
    }

    private Map<Integer, Set<StockDetail>> getSectorIdToStocksMap(Set<StockDetail> targetStockDetails) {
        Map<Integer, Set<StockDetail>> sectorIdToStockMap = Maps.newHashMap();
        targetStockDetails.forEach(stockDetail -> {
            sectorIdToStockMap.putIfAbsent(stockDetail.getSectorId(), Sets.newHashSet());
            sectorIdToStockMap.get(stockDetail.getSectorId()).add(stockDetail);
        });
        return sectorIdToStockMap;
    }
}
