package to.us.datagrip.accountability.models;


import to.us.datagrip.accountability.utils.PayloadRunnable;

public class MenuItem {
    public final static class TYPES {
        public static final int SECTION_MENU = 5;
        public static final int SECTION_CHECK = 6;
        public static final int SECTION_DESCRIPTION = 7;
        public static int SECTION_TITLE = 0;
        public static int SECTION_ONE_LINE = 1;
        public static int SECTION_DOUBLE_LINE = 2;
        public static int DIVIDER_THIN = 3;
        public static int DIVIDER_THICK = 4;
    }

    private String primaryText, secondaryText;
    private Object value;
    private PayloadRunnable action;
    private int type;
    private int icon;
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }

    public Object getValue() {
        return value;
    }

    public PayloadRunnable getAction() {
        return action;
    }

    public void setAction(PayloadRunnable action) {
        this.action = action;
    }

    public boolean getBoolean() {
        boolean value = false;
        try {
            value = (Boolean) this.value;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
