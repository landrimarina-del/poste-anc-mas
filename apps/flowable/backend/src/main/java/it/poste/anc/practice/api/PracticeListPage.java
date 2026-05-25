package it.poste.anc.practice.api;

import java.util.List;

public record PracticeListPage(
        List<PracticeListItem> items,
        long total,
        int page,
        int size
) {
}
