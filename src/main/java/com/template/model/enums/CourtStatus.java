package main.java.com.template.model.enums;

public enum CourtStatus {
    AVAILABLE("可預約", "✅"),
    BOOKED("已預約", "🎾"),
    MAINTENANCE("維修中", "🛠️");
//
    private final String label;
    private final String icon;
    CourtStatus(String label, String icon) { this.label = label; this.icon = icon; }
    public String getIcon() { return icon; }
    public String getLabel() { return label; }
}
