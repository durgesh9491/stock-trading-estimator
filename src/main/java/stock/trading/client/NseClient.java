package stock.trading.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import stock.trading.config.AppConfig;
import stock.trading.config.NseClientConfig;

import javax.inject.Inject;
import java.io.IOException;

public class NseClient extends BaseHttpClient {

    private NseClientConfig config;

    @Inject
    public NseClient(AppConfig appConfig) {
        super(appConfig);
        this.config = appConfig.getNseClientConfig();
    }

    public double getLatestStockPrice(String stock) {
        HttpGet httpGet = new HttpGet(config.getBaseUrl() + config.getLatestStockPriceEndPoint() + stock);
        try {
            setHeaders(httpGet);
            HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            return getStockPrice(responseBody);
        } catch (IOException ex) {
            System.out.println(String.format("Unable to get latest stock price for stock : %s", stock));
        } finally {
            httpGet.releaseConnection();
        }
        return Integer.MAX_VALUE;
    }

    private double getStockPrice(String responseBody) {
        String start = "\"lastPrice\":\"";
        String end = "\"}],\"";
        return Double.parseDouble(responseBody.substring(responseBody.indexOf(start) + start.length(), responseBody.indexOf("optLink") - end.length()).replaceAll(",", ""));
    }

    private void setHeaders(HttpRequestBase httpMethod) {
        httpMethod.setHeader("User-Agent", "PostmanRuntime/7.20.1");
    }
}
