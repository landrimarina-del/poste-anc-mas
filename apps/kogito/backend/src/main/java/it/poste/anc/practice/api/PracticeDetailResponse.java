package it.poste.anc.practice.api;

import java.time.Instant;
import java.time.LocalDate;

public record PracticeDetailResponse(
        Header header,
        Client client,
        BlockedCard blockedCard,
        String fase
) {
    public record Header(
            Long practiceId,
            String practiceNumber,
            String requestId,
            String idWorkItem,
            String state,
            String sdOutcome,
            Instant openedAt,
            Instant closedAt,
            Instant lastModifiedAt,
            String documentType,
            String koCodesJson
    ) {
    }

    public record Client(
            String firstName,
            String lastName,
            String fiscalCode,
            String customerCode,
            String gender,
            LocalDate birthDate,
            String birthCity,
            String birthProvince,
            String birthCountry,
            String phone,
            String mobilePhone,
            ResidenceAddress residenceAddress
    ) {
    }

    public record ResidenceAddress(
            String street,
            String city,
            String province,
            String country,
            String postalCode,
            String streetNumber
    ) {
    }

    public record BlockedCard(
            String cardNumberMasked,
            String cardType,
            String cardHolder
    ) {
    }
}
