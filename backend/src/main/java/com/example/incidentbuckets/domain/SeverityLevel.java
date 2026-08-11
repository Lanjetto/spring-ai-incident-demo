package com.example.incidentbuckets.domain;

/**
 * Шкала критичности инцидента.
 * Меньший номер = критичнее; A1 = FATAL, A4 = спокойный.
 */
public enum SeverityLevel {
    A1("FATAL", "ресурс недоступен, пользователи не могут работать"),
    A2("HIGH", "серьёзная деградация, часть функционала недоступна"),
    A3("MEDIUM", "заметная деградация, работа затруднена"),
    A4("LOW", "сервисы работают, но у пользователя есть проблемы");

    private final String label;
    private final String description;

    SeverityLevel(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
