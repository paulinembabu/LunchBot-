package com.bot;

import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({BotController.class})

public class Application extends BotDependencyConfiguration {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Returns a custom Adapter that provides error handling.
     *
     * @param configuration The Configuration object to use.
     * @return An error handling BotFrameworkHttpAdapter.
     */
    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new AdapterWithErrorHandler(configuration);
    }
}

