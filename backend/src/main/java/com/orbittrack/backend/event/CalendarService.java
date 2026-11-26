package com.orbittrack.backend.event;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class CalendarService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneOffset.UTC);

    public String generate(EventResponse event) {
        StringBuilder calendar = new StringBuilder();

        appendLine(calendar, "BEGIN:VCALENDAR");
        appendLine(calendar, "VERSION:2.0");
        appendLine(calendar, "PRODID:-//Orbit Track//Agenda//FR");
        appendLine(calendar, "CALSCALE:GREGORIAN");
        appendLine(calendar, "BEGIN:VEVENT");

        appendLine(calendar, "UID:event-" + event.id() + "@orbittrack");
        appendLine(calendar, "DTSTAMP:" + FORMATTER.format(Instant.now()));
        appendLine(calendar, "DTSTART:" + FORMATTER.format(event.startAt()));

        if (event.endAt() != null && event.endAt().isAfter(event.startAt())) {
            appendLine(calendar, "DTEND:" + FORMATTER.format(event.endAt()));
        }

        appendLine(calendar, "SUMMARY:" + escape(event.name()));
        appendLine(calendar, "DESCRIPTION:" + escape(event.description()));
        appendLine(calendar, "LOCATION:" + escape(event.visibilityZone()));
        appendLine(calendar, "STATUS:CONFIRMED");

        appendLine(calendar, "END:VEVENT");
        appendLine(calendar, "END:VCALENDAR");

        return calendar.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "\\n")
                .replace(";", "\\;")
                .replace(",", "\\,");
    }

    private void appendLine(StringBuilder calendar, String line) {
        int bytes = 0;

        for (int i = 0; i < line.length();) {
            int codePoint = line.codePointAt(i);
            String character = new String(Character.toChars(codePoint));
            int length = character.getBytes(StandardCharsets.UTF_8).length;

            if (bytes + length > 75) {
                calendar.append("\r\n ");
                bytes = 1;
            }

            calendar.append(character);
            bytes += length;
            i += Character.charCount(codePoint);
        }

        calendar.append("\r\n");
    }
}
