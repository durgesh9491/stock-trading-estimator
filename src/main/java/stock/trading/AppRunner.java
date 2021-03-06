package stock.trading;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import stock.trading.config.AppConfig;
import stock.trading.service.StockTradingEngine;

import java.util.Scanner;

@Slf4j
public class AppRunner extends Application<AppConfig> {
    private static Injector injector;
    private static final String appName = "Stock Trading Estimator (STE)";

    public static void main(String[] args) {
        try {
            new AppRunner().run(args);
        } catch (Exception ex) {
            log.error("Unable to start : {}, exiting!", AppRunner.class.getSimpleName(), ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void run(AppConfig appConfig, Environment environment) {
        initializeGuice(appConfig, environment);
        start();
    }

    @Override
    public String getName() {
        return appName;
    }

    public static Injector getInjector() {
        return injector;
    }

    private void start() {
        StockTradingEngine tradingEngine = getInjector().getInstance(StockTradingEngine.class);
        tradingEngine.run();
    }

    private void initializeGuice(AppConfig appConfig, Environment environment) {
        AppModule appModule = new AppModule(environment, appConfig);
        injector = Guice.createInjector(appModule);
    }
}
