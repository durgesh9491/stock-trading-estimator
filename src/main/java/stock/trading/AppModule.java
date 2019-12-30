package stock.trading;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import stock.trading.service.StockTradingEngine;

public class AppModule implements Module {
    @Provides
    @Singleton
    StockTradingEngine provideStockTradingApp() {
        return new StockTradingEngine();
    }

    @Override
    public void configure(Binder binder) {

    }
}
