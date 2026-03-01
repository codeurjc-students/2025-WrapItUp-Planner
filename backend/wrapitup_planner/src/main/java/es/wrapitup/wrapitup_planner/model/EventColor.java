package es.wrapitup.wrapitup_planner.model;

public enum EventColor {
    BLUE("#3B82F6"),
    GREEN("#10B981"),
    RED("#EF4444"),
    YELLOW("#F59E0B"),
    PURPLE("#8B5CF6");
    
    private final String hexCode;
    
    EventColor(String hexCode) {
        this.hexCode = hexCode;
    }
    
    public String getHexCode() {
        return hexCode;
    }
}
