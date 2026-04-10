package com.repairshop.app.communication;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class RepairCommunicationLinkService {

    private static final String DEFAULT_COUNTRY_DIAL_CODE = "212";

    private final MessageSource messageSource;

    public RepairCommunicationLinkService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public RepairMessageLinksView buildTrackingLinks(
            RepairCommunicationContext context,
            Locale locale,
            String applicationBaseUrl
    ) {
        String selectedPhone = selectPreferredPhone(context.primaryPhone(), context.secondaryPhone());
        if (selectedPhone == null) {
            return unavailable();
        }

        String message = buildTrackingMessage(context, locale, applicationBaseUrl);
        return new RepairMessageLinksView(
                selectedPhone,
                buildWhatsAppUrl(selectedPhone, message),
                buildSmsUrl(selectedPhone, message)
        );
    }

    public RepairMessageLinksView buildReadyForPickupLinks(
            RepairCommunicationContext context,
            Locale locale,
            String applicationBaseUrl
    ) {
        String selectedPhone = selectPreferredPhone(context.primaryPhone(), context.secondaryPhone());
        if (selectedPhone == null) {
            return unavailable();
        }

        String message = buildReadyForPickupMessage(context, locale, applicationBaseUrl);
        return new RepairMessageLinksView(
                selectedPhone,
                buildWhatsAppUrl(selectedPhone, message),
                buildSmsUrl(selectedPhone, message)
        );
    }

    private RepairMessageLinksView unavailable() {
        return new RepairMessageLinksView(null, null, null);
    }

    private String buildTrackingMessage(
            RepairCommunicationContext context,
            Locale locale,
            String applicationBaseUrl
    ) {
        String trackingLink = buildPublicTrackingLink(applicationBaseUrl, context.publicTrackingToken());
        if (hasText(context.customerName())) {
            return messageSource.getMessage(
                    "communication.message.tracking.named",
                    new Object[]{context.customerName(), context.repairTitle(), context.shopBusinessName(), trackingLink},
                    locale
            );
        }
        return messageSource.getMessage(
                "communication.message.tracking.generic",
                new Object[]{context.repairTitle(), context.shopBusinessName(), trackingLink},
                locale
        );
    }

    private String buildReadyForPickupMessage(
            RepairCommunicationContext context,
            Locale locale,
            String applicationBaseUrl
    ) {
        String trackingLink = buildPublicTrackingLink(applicationBaseUrl, context.publicTrackingToken());
        if (hasText(context.customerName())) {
            return messageSource.getMessage(
                    "communication.message.pickup.named",
                    new Object[]{context.customerName(), context.repairTitle(), context.shopBusinessName(), context.pickupCode(), trackingLink},
                    locale
            );
        }
        return messageSource.getMessage(
                "communication.message.pickup.generic",
                new Object[]{context.repairTitle(), context.shopBusinessName(), context.pickupCode(), trackingLink},
                locale
        );
    }

    private String buildPublicTrackingLink(String applicationBaseUrl, String publicTrackingToken) {
        return UriComponentsBuilder.fromUriString(applicationBaseUrl)
                .path("/track/{token}")
                .buildAndExpand(publicTrackingToken)
                .toUriString();
    }

    private String buildWhatsAppUrl(String phoneNumber, String message) {
        String targetPhone = normalizeForWhatsApp(phoneNumber);
        if (targetPhone == null) {
            return null;
        }
        return "https://wa.me/" + targetPhone + "?text=" + UriUtils.encodeQueryParam(message, StandardCharsets.UTF_8);
    }

    private String buildSmsUrl(String phoneNumber, String message) {
        String targetPhone = normalizeForSms(phoneNumber);
        if (targetPhone == null) {
            return null;
        }
        return "sms:" + targetPhone + "?body=" + UriUtils.encodeQueryParam(message, StandardCharsets.UTF_8);
    }

    private String selectPreferredPhone(String primaryPhone, String secondaryPhone) {
        if (hasText(primaryPhone)) {
            return primaryPhone.trim();
        }
        if (hasText(secondaryPhone)) {
            return secondaryPhone.trim();
        }
        return null;
    }

    private String normalizeForWhatsApp(String phoneNumber) {
        if (!hasText(phoneNumber)) {
            return null;
        }

        String trimmed = phoneNumber.trim();
        String digits = normalizeDialableDigits(trimmed);
        if (digits.isEmpty()) {
            return null;
        }
        return digits;
    }

    private String normalizeForSms(String phoneNumber) {
        if (!hasText(phoneNumber)) {
            return null;
        }

        String trimmed = phoneNumber.trim();
        if (trimmed.startsWith("+")) {
            String digits = trimmed.substring(1).replaceAll("\\D+", "");
            return digits.isEmpty() ? null : "+" + digits;
        }

        String digits = normalizeDialableDigits(trimmed);
        if (digits.isEmpty()) {
            return null;
        }
        return "+" + digits;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeDialableDigits(String phoneNumber) {
        String digits = phoneNumber.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return "";
        }
        if (phoneNumber.startsWith("00") && digits.length() > 2) {
            return digits.substring(2);
        }
        if (!phoneNumber.startsWith("+") && digits.startsWith("0") && digits.length() == 10) {
            return DEFAULT_COUNTRY_DIAL_CODE + digits.substring(1);
        }
        return digits;
    }
}
