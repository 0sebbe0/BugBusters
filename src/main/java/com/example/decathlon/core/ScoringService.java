package com.example.decathlon.core;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ScoringService {
    public enum Type { TRACK, FIELD }
    public record EventDef(String id, Type type, double A, double B, double C, String unit) {}

    private final Map<String, EventDef> dec = Map.ofEntries(
            Map.entry("100m", new EventDef("100m", Type.TRACK, 25.4347, 18.0, 1.81, "s")),
            Map.entry("longJump", new EventDef("longJump", Type.FIELD, 0.14354, 220.0, 1.40, "cm")),
            Map.entry("shotPut", new EventDef("shotPut", Type.FIELD, 51.39, 1.5, 1.05, "m")),
            Map.entry("highJump", new EventDef("highJump", Type.FIELD, 0.8465, 75.0, 1.42, "cm")),
            Map.entry("400m", new EventDef("400m", Type.TRACK, 1.53775, 82.0, 1.81, "s")),
            Map.entry("110mHurdles", new EventDef("110mHurdles", Type.TRACK, 5.74352, 28.5, 1.92, "s")),
            Map.entry("discus", new EventDef("discus", Type.FIELD, 12.91, 4.0, 1.10, "m")),
            Map.entry("poleVault", new EventDef("poleVault", Type.FIELD, 0.2797, 100.0, 1.35, "cm")),
            Map.entry("javelin", new EventDef("javelin", Type.FIELD, 10.14, 7.0, 1.08, "m")),
            Map.entry("1500m", new EventDef("1500m", Type.TRACK, 0.03768, 480.0, 1.85, "s"))
    );

    private final Map<String, EventDef> hep = Map.ofEntries(
            Map.entry("100mHurdles", new EventDef("100mHurdles", Type.TRACK, 9.23076, 26.7, 1.835, "s")),
            Map.entry("highJump", new EventDef("highJump", Type.FIELD, 1.84523, 75.0, 1.348, "cm")),
            Map.entry("shotPut", new EventDef("shotPut", Type.FIELD, 56.0211, 1.5, 1.05, "m")),
            Map.entry("200m", new EventDef("200m", Type.TRACK, 4.99087, 42.5, 1.81, "s")),
            Map.entry("longJump", new EventDef("longJump", Type.FIELD, 0.188807, 210.0, 1.41, "cm")),
            Map.entry("javelin", new EventDef("javelin", Type.FIELD, 15.9803, 3.8, 1.04, "m")),
            Map.entry("800m", new EventDef("800m", Type.TRACK, 0.11193, 254.0, 1.88, "s"))
    );

    // Exakta gränser (inkl. dec & hep)
    private final Map<String, double[]> decLimits = Map.ofEntries(
            Map.entry("100m",        new double[]{5,   20}),
            Map.entry("110mHurdles", new double[]{10,  30}),
            Map.entry("400m",        new double[]{20, 100}),
            Map.entry("1500m",       new double[]{150, 400}),
            Map.entry("discus",      new double[]{0,   85}),
            Map.entry("highJump",    new double[]{0,   300}),
            Map.entry("javelin",     new double[]{0,   110}),
            Map.entry("longJump",    new double[]{0,   1000}),
            Map.entry("poleVault",   new double[]{0,   1000}),
            Map.entry("shotPut",     new double[]{0,   30})
    );

    private final Map<String, double[]> hepLimits = Map.ofEntries(
            Map.entry("100mHurdles", new double[]{10, 30}),
            Map.entry("200m",        new double[]{20, 100}),
            Map.entry("800m",        new double[]{70, 250}),
            Map.entry("highJump",    new double[]{0,  300}),
            Map.entry("javelin",     new double[]{0,  110}),
            Map.entry("longJump",    new double[]{0,  1000}),
            Map.entry("shotPut",     new double[]{0,  30})
    );

    private void validate(String mode, String eventId, double raw) {
        Map<String, double[]> limits = "HEP".equals(mode) ? hepLimits : decLimits;
        double[] lm = limits.get(eventId);
        if (lm != null) {
            double lo = lm[0], hi = lm[1];
            if (raw < lo) throw new IllegalArgumentException("Value too low");
            if (raw > hi) throw new IllegalArgumentException("Value too high");
        }
    }

    public int score(String mode, String eventId, double raw) {
        Map<String, EventDef> m = "HEP".equals(mode) ? hep : dec;
        EventDef e = m.get(eventId);
        if (e == null) return 0;

        validate(mode, eventId, raw);

        double points;
        if (e.type == Type.TRACK) {
            double x = e.B - raw;
            if (x <= 0) return 0;
            points = e.A * Math.pow(x, e.C);
        } else {
            double x = raw - e.B;
            if (x <= 0) return 0;
            points = e.A * Math.pow(x, e.C);
        }
        return (int) Math.floor(points);
    }
}
