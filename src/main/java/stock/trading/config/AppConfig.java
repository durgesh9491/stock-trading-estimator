package stock.trading.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Data;

import javax.inject.Singleton;

/**
 * Created By durgesh.soni on 31/12/19
 */

@Data
@Singleton
public class AppConfig extends Configuration {

    @JsonProperty("nseDataScrapper")
    private NseClientConfig nseClientConfig;

    @JsonProperty("baseHttpClient")
    private BaseHttpClientConfig httpClientConfig;
}
