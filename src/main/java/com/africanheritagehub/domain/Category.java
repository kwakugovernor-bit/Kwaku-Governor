package com.africanheritagehub.domain;

/** Skill categories. Display names live in messages*.properties as category.{NAME}. */
public enum Category {
    NURSE("🩺"), TEACHER("📚"), CAREGIVER("🤝"), MASON("🧱"), CARPENTER("🪚"),
    ELECTRICIAN("💡"), PLUMBER("🔧"), PAINTER("🎨"), WELDER("🔥"), TILER("🏠"),
    MECHANIC("🛠️"), DRIVER("🚗"), TRUCK_OWNER("🚚"), SEAMSTRESS("🧵"), HAIRDRESSER("💇"),
    CHEF_CATERER("🍲"), CLEANER("🧹"), SECURITY_GUARD("🛡️"), FARM_HAND("🌾"),
    PHOTOGRAPHER("📷"), OTHER("⭐");

    private final String icon;

    Category(String icon) { this.icon = icon; }

    public String getIcon() { return icon; }
}
