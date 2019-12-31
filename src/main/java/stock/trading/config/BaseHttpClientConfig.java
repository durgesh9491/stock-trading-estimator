package stock.trading.config;

import lombok.Getter;

import javax.inject.Singleton;

/**
 * Created By durgesh.soni on 31/12/19
 */

@Getter
@Singleton
public class BaseHttpClientConfig {
    private int timeout;
    private int connectionTimeout;
    private int timeToLive;
    private boolean cookiesEnabled;
    private int maxConnections;
    private int maxConnectionsPerRoute;
    private int keepAlive;
    private int retries;
    private int socketTimeout;
    private boolean retry;
}
