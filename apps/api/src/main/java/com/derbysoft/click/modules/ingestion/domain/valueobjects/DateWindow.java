package com.derbysoft.click.modules.ingestion.domain.valueobjects;

import java.time.LocalDate;

public record DateWindow(LocalDate from, LocalDate to) {

    public DateWindow {
        if (from == null || to == null) {
            throw new IllegalArgumentException("DateWindow from and to must not be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("DateWindow from must not be after to");
        }
    }

    public String toKey() {
        return from + "_" + to;
    }

    public long days() {
        return to.toEpochDay() - from.toEpochDay() + 1;
    }
}
