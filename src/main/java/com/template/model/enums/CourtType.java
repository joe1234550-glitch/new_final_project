package main.java.com.template.model.enums;

public enum CourtType {
    HARD("硬地", "🎾"),
    CLAY("紅土", "🧱"),
    GRASS("草地", "🌱");

    private final String label;
    private final String icon;
    CourtType(String label, String icon) { this.label = label; this.icon = icon; }
    public String getIcon() { return icon; }
    public String getLabel() { return label; }
}
