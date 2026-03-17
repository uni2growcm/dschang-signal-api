package u2g.codylab.dschang_signal.service;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;


@Service
public class I18nService {

    private final MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    public String get(String key, Object... args) {
        Object[] stringArgs = java.util.Arrays.stream(args)
                .map(Object::toString)
                .toArray();
        return messageSource.getMessage(key, stringArgs, LocaleContextHolder.getLocale());
    }


    public String getOrDefault(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}