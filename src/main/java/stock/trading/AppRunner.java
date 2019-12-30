package stock.trading;

import com.google.inject.Guice;
import com.google.inject.Injector;
import stock.trading.service.StockTradingEngine;

import java.util.Scanner;

public class AppRunner {
    private static Injector injector;

    public static void main(String[] args) {
        System.out.println("-----------------Application has been started!-----------------");

        initializeGuice();
        run();
    }

    private static Injector getInjector() {
        return injector;
    }

    private static void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter your capital : ");
        double totalCapital = scanner.nextDouble();

        StockTradingEngine tradingEngine = getInjector().getInstance(StockTradingEngine.class);
        tradingEngine.run(totalCapital);
    }

    private static void initializeGuice() {
        AppModule appModule = new AppModule();
        injector = Guice.createInjector(appModule);
    }
}
