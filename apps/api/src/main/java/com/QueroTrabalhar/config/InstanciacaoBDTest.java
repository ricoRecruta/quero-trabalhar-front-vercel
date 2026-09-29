package com.QueroTrabalhar.config;

import com.QueroTrabalhar.services.bd.SemeadorBd;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class InstanciacaoBDTest implements ApplicationRunner {

    private final SemeadorBd semeadorBd;

    public InstanciacaoBDTest(SemeadorBd semeadorBd) {
        this.semeadorBd = semeadorBd;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.semeadorBd.popularBd();
    }
}
