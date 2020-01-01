package stock.trading.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import stock.trading.bean.SectorDetail;
import stock.trading.bean.StockDetail;
import stock.trading.client.NseClient;
import stock.trading.config.AppConfig;
import stock.trading.config.NseClientConfig;
import stock.trading.constant.FileName;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
class TradingEstimateService {

    @Inject
    private NseClient nseClient;

    private BufferedReader reader;
    private NseClientConfig config;

    @Inject
    TradingEstimateService(AppConfig appConfig) {
        this.config = appConfig.getNseClientConfig();
    }

    double calculateTradingAmount(Set<SectorDetail> targetSectorDetails, Map<Integer, Set<StockDetail>> sectorToStocksMap) {
        System.out.println("\n\n-------------- Sector wise target trading amount --------------\n");

        Map<Integer, SectorDetail> sectorDetailMap = getSectorIdToObjMap(targetSectorDetails);
        double totalAmount = sectorToStocksMap.entrySet().stream().map(entry -> {
            double sectorWiseAmount = entry.getValue().stream().map(stock -> stock.getPrice() * stock.getUnits()).mapToDouble(Double::doubleValue).sum();
            System.out.println(String.format("Sector : %s | Target amount : %.2f", sectorDetailMap.get(entry.getKey()).getName(), sectorWiseAmount));
            return sectorWiseAmount;
        }).mapToDouble(Double::doubleValue).sum();

        System.out.println(String.format("\n\nTotal trading amount : %.2f", totalAmount));
        return totalAmount;
    }

    Map<Integer, Set<StockDetail>> allocateStocks(Set<SectorDetail> targetSectorDetails, Set<StockDetail> targetStockDetails, double totalCapital) {
        Map<Integer, SectorDetail> idToSectorDetailMap = getSectorIdToObjMap(targetSectorDetails);
        List<Integer> prioritisedSectorIds = getPrioritisedSectorIds(idToSectorDetailMap);
        Map<Integer, Set<StockDetail>> sectorIdToStocksMap = getSectorIdToStocksMap(targetStockDetails);

        System.out.println("\n\n-------------- Stock wise target trading amount --------------");

        prioritisedSectorIds.forEach(sectorId -> {
            SectorDetail sectorDetail = idToSectorDetailMap.get(sectorId);
            double sectorWeight = sectorDetail.getWeight();
            Set<StockDetail> stockDetails = sectorIdToStocksMap.get(sectorId);
            System.out.println();

            if (Objects.isNull(stockDetails)) {
                System.out.println(String.format("**** No target stocks found for sector : %s", sectorDetail.getName()));
                return;
            }
            stockDetails = stockDetails.stream().collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(StockDetail::getDisplayName))));
            stockDetails.forEach(stock -> {
                double stockWeight = stock.getWeight();
                int units = (int) ((totalCapital * sectorWeight * stockWeight) / stock.getPrice());
                stock.setUnits(stock.getUnits() + units);
                System.out.println(String.format("Sector : %s | Stock : %s | Price : %s | Units : %s", sectorDetail.getName(), stock.getDisplayName(), stock.getPrice(), stock.getUnits()));
            });
        });
        return sectorIdToStocksMap;
    }

    Set<SectorDetail> getTargetSectorDetails() throws IOException {
        Set<SectorDetail> sectorDetails = Sets.newHashSet();
        String targetSector = "";
        try (FileReader fileReader = new FileReader(config.getDataFileBasePath() + FileName.nseTargetSectorsFile.getValue())) {
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
        } catch (IOException ex) {
            System.out.println("Unable to complete the process for sector : " + targetSector);
            throw ex;
        }
        return sectorDetails;
    }

    Set<StockDetail> getTargetStockDetails() throws IOException {
        Set<StockDetail> stockDetails = Sets.newHashSet();
        String targetStock = "";
        try (FileReader fileReader = new FileReader(config.getDataFileBasePath() + FileName.nseTargetStocksFile.getValue())) {
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
        } catch (IOException ex) {
            System.out.println("Unable to complete the process for stock : " + targetStock);
            throw ex;
        }
        return stockDetails;
    }

    Set<StockDetail> getApplicableStockDetails(Set<SectorDetail> targetSectorDetails, Set<StockDetail> stockDetails) {
        Map<Integer, SectorDetail> sectorDetailMap = getSectorIdToObjMap(targetSectorDetails);
        return stockDetails.
                parallelStream().
                filter(stockDetail -> (stockDetail.isApplicable() & sectorDetailMap.containsKey(stockDetail.getSectorId()))).
                collect(Collectors.toSet());
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
        System.out.println(String.format("\n\nTime taken to get latest stock prices : %s seconds", (System.currentTimeMillis() - startTime) / TimeUnit.SECONDS.toMillis(1)));
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

    private List<Integer> getPrioritisedSectorIds(Map<Integer, SectorDetail> idToSectorDetailMap) {
        List<Integer> sectorIds = new ArrayList<>(idToSectorDetailMap.keySet());
        sectorIds.sort((o1, o2) -> idToSectorDetailMap.get(o2).getWeight().compareTo(idToSectorDetailMap.get(o1).getWeight()));
        return sectorIds;
    }
}
