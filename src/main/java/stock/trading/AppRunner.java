package stock.trading;

import com.google.inject.Guice;
import com.google.inject.Injector;
import stock.trading.service.StockTradingEngine;

import java.util.Scanner;

public class AppRunner {
    private static Injector injector;

    public static void main(String[] args) {
        run();
        System.out.println("Please enter your capital : ");
        Scanner scanner = new Scanner(System.in);
        double totalCapital = scanner.nextDouble();

        StockTradingEngine tradingEngine = getInjector().getInstance(StockTradingEngine.class);
        tradingEngine.run(totalCapital);
    }

    public static Injector getInjector() {
        return injector;
    }

    private static void run() {
        AppModule appModule = new AppModule();
        injector = Guice.createInjector(appModule);
    }
}
