package stock.trading.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import stock.trading.constant.AppConstant;

import java.io.IOException;

@Slf4j
public class NseClient {
    private HttpClient client = HttpClientBuilder.
            create().
            setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).
            build();

    public double getLatestStockPrice(String stock) {
        HttpGet httpGet = new HttpGet(AppConstant.NSE_CLIENT_BASE_URL + stock);
        try {
            setHeaders(httpGet);
            HttpResponse response = client.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            return getStockPrice(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpGet.releaseConnection();
        }
        return (1L << 63);
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
