package stock.trading.client;

import io.dropwizard.lifecycle.Managed;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import stock.trading.config.AppConfig;
import stock.trading.config.BaseHttpClientConfig;

import javax.inject.Inject;

/**
 * Created By durgesh.soni on 31/12/19
 */

@Slf4j
@NoArgsConstructor
public abstract class BaseHttpClient implements Managed {
    protected CloseableHttpClient httpClient;
    private BaseHttpClientConfig httpClientConfig;
    private PoolingHttpClientConnectionManager connectionManager;

    @Inject
    BaseHttpClient(AppConfig appConfig) {
        this.httpClientConfig = appConfig.getHttpClientConfig();
        start();
    }

    @Override
    public void start() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(httpClientConfig.getMaxConnectionsPerRoute());
        connectionManager.setMaxTotal(httpClientConfig.getMaxConnections());

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
                setConnectionManager(connectionManager).
                setDefaultRequestConfig(buildRequestConfig());

        if (httpClientConfig.isRetry()) {
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(httpClientConfig.getRetries(), httpClientConfig.isRetry()));
        }
        httpClient = httpClientBuilder.build();
    }

    @Override
    public void stop() {
        connectionManager.close();
    }

    private RequestConfig buildRequestConfig() {
        return RequestConfig.
                custom().
                setConnectTimeout(httpClientConfig.getTimeout()).
                setCookieSpec(CookieSpecs.STANDARD).
                setSocketTimeout(httpClientConfig.getSocketTimeout()).
                setConnectionRequestTimeout(httpClientConfig.getConnectionTimeout()).
                setRedirectsEnabled(httpClientConfig.isRetry()).
                setMaxRedirects(httpClientConfig.getRetries()).
                build();
    }
}
