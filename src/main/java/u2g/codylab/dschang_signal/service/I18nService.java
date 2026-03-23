package u2g.codylab.dschang_signal.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Service
public class I18nService {

    private final MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key, Object... args) {
        Locale locale = resolveLocale();
        return messageSource.getMessage(key, args, locale);
    }

    private Locale resolveLocale() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                System.out.println(">>> [I18N] attrs NULL → defaulting to EN");
                return Locale.ENGLISH;
            }

            HttpServletRequest request = attrs.getRequest();
            String acceptLanguage = request.getHeader("Accept-Language");
            System.out.println(">>> [I18N] Accept-Language header = " + acceptLanguage);

            if (acceptLanguage != null && acceptLanguage.toLowerCase().startsWith("fr")) {
                return Locale.FRENCH;
            }
            return Locale.ENGLISH;
        } catch (Exception e) {
            System.out.println(">>> [I18N] Exception: " + e.getMessage());
            return Locale.ENGLISH;
        }
    }
}