package io.github.bsayli.licensing.common.i18n.locale.impl;

import io.github.bsayli.licensing.common.i18n.locale.CurrentLocaleProvider;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DefaultLocaleProvider implements CurrentLocaleProvider {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Override
    public Locale getCurrentLocale() {
        return DEFAULT_LOCALE;
    }
}
