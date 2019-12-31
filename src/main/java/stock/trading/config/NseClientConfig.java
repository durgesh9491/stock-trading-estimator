package stock.trading.config;

import lombok.Getter;

import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;

/**
 * Created By durgesh.soni on 31/12/19
 */

@Getter
@Singleton
public class NseClientConfig {

    @NotBlank
    private String baseUrl;
    @NotBlank
    private String latestStockPriceEndPoint;
    @NotBlank
    private String dataFileBasePath;
}
