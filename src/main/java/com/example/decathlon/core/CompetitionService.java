package com.example.decathlon.core;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CompetitionService {
    private final ScoringService scoring;

    public CompetitionService(ScoringService scoring) {
        this.scoring = scoring;
    }

    public static class Competitor {
        public final String name;
        public final Map<String, Map<String, Integer>> pointsByMode = new ConcurrentHashMap<>();
        public Competitor(String name) { this.name = name; }
        public int total(String mode) {
            return pointsByMode.getOrDefault(mode, Map.of()).values().stream().mapToInt(i -> i).sum();
        }
    }

    private final Map<String, Competitor> competitors = new LinkedHashMap<>();

    public synchronized void addCompetitor(String name) {
        competitors.computeIfAbsent(name, Competitor::new);
    }

    public synchronized int score(String name, String mode, String eventId, double raw) {
        Competitor c = competitors.computeIfAbsent(name, Competitor::new);
        int pts = scoring.score(mode, eventId, raw);
        c.pointsByMode.computeIfAbsent(mode, k -> new ConcurrentHashMap<>()).put(eventId, pts);
        return pts;
    }

    public synchronized List<Map<String, Object>> standings(String mode) {
        return competitors.values().stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", c.name);
                    m.put("scores", new LinkedHashMap<>(c.pointsByMode.getOrDefault(mode, Map.of())));
                    m.put("total", c.total(mode));
                    return m;
                })
                .sorted(Comparator.comparingInt(m -> -((Integer) m.get("total"))))
                .collect(Collectors.toList());
    }

    public synchronized String exportCsv(String mode) {
        Set<String> eventIds = new LinkedHashSet<>();
        competitors.values().forEach(c -> eventIds.addAll(c.pointsByMode.getOrDefault(mode, Map.of()).keySet()));
        List<String> header = new ArrayList<>();
        header.add("Name");
        header.addAll(eventIds);
        header.add("Total");
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", header)).append("\n");
        for (Competitor c : competitors.values()) {
            List<String> row = new ArrayList<>();
            row.add(c.name);
            int sum = 0;
            Map<String,Integer> map = c.pointsByMode.getOrDefault(mode, Map.of());
            for (String ev : eventIds) {
                Integer p = map.get(ev);
                row.add(p == null ? "" : String.valueOf(p));
                if (p != null) sum += p;
            }
            row.add(String.valueOf(sum));
            sb.append(String.join(",", row)).append("\n");
        }
        return sb.toString();
    }

    public synchronized int count() { return competitors.size(); }
}
