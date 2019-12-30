package stock.trading.validation;

import java.util.List;
import java.util.Map;

public class DataValidator {

    private static final Double TARGET_ALL_ALLOCATION = 1.0;

    public static void validateStockAllocation(Map<Integer, List<Double>> sectorWiseAllocationMap) {
        sectorWiseAllocationMap.forEach((key, value) -> {
            Double totalAllocation = value.stream().mapToDouble(Double::doubleValue).sum();
            if (!totalAllocation.equals(TARGET_ALL_ALLOCATION)) {
                throw new RuntimeException(String.format("Sector wise allocation is incorrect for sectorId  : %s", key));
            }
        });
    }

    public static void validateSectorAllocation(List<Double> allocationList) {
        Double totalAllocation = allocationList.stream().mapToDouble(Double::doubleValue).sum();
        if (!totalAllocation.equals(TARGET_ALL_ALLOCATION)) {
            throw new RuntimeException("Sector allocation is incomplete, please correct!");
        }
    }
}
