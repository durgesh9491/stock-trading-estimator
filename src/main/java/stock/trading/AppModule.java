package stock.trading;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.dropwizard.setup.Environment;
import lombok.Data;
import stock.trading.config.AppConfig;

@Data
public class AppModule implements Module {
    private final Environment environment;
    private final AppConfig configuration;

    @Override
    public void configure(Binder binder) {
        binder.bind(AppConfig.class).toInstance(configuration);
        binder.bind(Environment.class).toInstance(environment);
    }
}
