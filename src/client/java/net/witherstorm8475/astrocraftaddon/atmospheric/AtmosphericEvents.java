package net.witherstorm8475.astrocraftaddon.atmospheric;

import com.google.gson.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AtmosphericEvents {

    private static final String[] CONFIG_CANDIDATES = new String[] {
            "run/config/astrocraft-addon.json",
            "config/astrocraft-addon.json",
            "run/config/astrocraft-addon/astrocraft-addon.json",
            "run/config/astrocraft-addon/atmospheric.json"
    };

    private static Map<String, PlanetAtmosphere> ATMOSPHERE_MAP = null;
    private static final Map<String, ForestFireState> FIRE_STATES = new HashMap<>();
    private static final Random random = new Random();

    // ---------- Data classes ----------
    public static class PlanetAtmosphere {
        public final ForestFireConfig forestFires;
        public final SkyColorsConfig skyColors;
        public PlanetAtmosphere(ForestFireConfig fires, SkyColorsConfig colors) {
            this.forestFires = fires;
            this.skyColors = colors;
        }
        @Override public String toString() {
            return "PlanetAtmosphere{fires=" + forestFires + ", colors=" + skyColors + "}";
        }
    }

    public static class ForestFireConfig {
        public final boolean enabled;
        public final double minDuration;
        public final double maxDuration;
        public final double minInterval;
        public final double maxInterval;
        public final int skyTintStart;
        public final int skyTintEnd;
        public final double maxTintStrength;
        public final double spreadRadius;

        public ForestFireConfig(boolean enabled, double minDur, double maxDur,
                                double minInt, double maxInt, int tintStart, int tintEnd,
                                double tintStrength, double radius) {
            this.enabled = enabled;
            this.minDuration = minDur;
            this.maxDuration = maxDur;
            this.minInterval = minInt;
            this.maxInterval = maxInt;
            this.skyTintStart = tintStart;
            this.skyTintEnd = tintEnd;
            this.maxTintStrength = tintStrength;
            this.spreadRadius = radius;
        }

        @Override public String toString() {
            return "ForestFireConfig{enabled=" + enabled + ",minDur=" + minDuration + ",maxDur=" + maxDuration +
                    ",minInt=" + minInterval + ",maxInt=" + maxInterval + ",tintStart=0x" + Integer.toHexString(skyTintStart) +
                    ",tintEnd=0x" + Integer.toHexString(skyTintEnd) + ",maxTint=" + maxTintStrength + ",radius=" + spreadRadius + "}";
        }
    }

    public static class ForestFireState {
        public boolean active;
        public double startTime;
        public double endTime;
        public double nextFireTime;
        public double intensity;
        public double centerX;
        public double centerZ;
        public ForestFireState() { this.active = false; this.startTime = 0; this.endTime = 0; this.nextFireTime = 0; this.intensity = 0; this.centerX = 0; this.centerZ = 0; }
    }

    public static class SkyColorsConfig {
        public final int sunriseColor;
        public final int sunsetColor;
        public final int dayColor;
        public final int nightColor;
        public final int horizonColor;
        public SkyColorsConfig(int sunrise, int sunset, int day, int night, int horizon) {
            this.sunriseColor = sunrise; this.sunsetColor = sunset; this.dayColor = day; this.nightColor = night; this.horizonColor = horizon;
        }
        @Override public String toString() {
            return "SkyColors{sunrise=0x" + Integer.toHexString(sunriseColor) +
                    ",sunset=0x" + Integer.toHexString(sunsetColor) +
                    ",day=0x" + Integer.toHexString(dayColor) +
                    ",night=0x" + Integer.toHexString(nightColor) +
                    ",horizon=0x" + Integer.toHexString(horizonColor) + "}";
        }
    }

    // ---------- Loading / parsing ----------
    public static synchronized void load() {
        // initialize map
        ATMOSPHERE_MAP = new HashMap<>();

        JsonObject root = null;
        File usedFile = null;

        // 1) try classpath variants first (if someone packaged the file)
        try {
            InputStream is = AtmosphericEvents.class.getResourceAsStream("/config/astrocraft-addon.json");
            if (is == null) is = AtmosphericEvents.class.getClassLoader().getResourceAsStream("config/astrocraft-addon.json");
            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is);
                     BufferedReader br = new BufferedReader(isr)) {
                    root = JsonParser.parseReader(br).getAsJsonObject();
                }
            }
        } catch (Exception ignored) {
        }

        // 2) try filesystem candidate paths
        if (root == null) {
            for (String path : CONFIG_CANDIDATES) {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    try (FileReader fr = new FileReader(f);
                         BufferedReader br = new BufferedReader(fr)) {
                        root = JsonParser.parseReader(br).getAsJsonObject();
                        usedFile = f;
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (root == null) {
            return;
        }

        // parse visual.planets and visual.moons (supports both nested and flat formats)
        try {
            if (root.has("visual") && root.get("visual").isJsonObject()) {
                JsonObject visual = root.getAsJsonObject("visual");

                if (visual.has("planets") && visual.get("planets").isJsonArray()) {
                    parsePlanetsArray(visual.getAsJsonArray("planets"));
                } else if (root.has("planets") && root.get("planets").isJsonArray()) {
                    // fall back if user placed planets at top-level
                    parsePlanetsArray(root.getAsJsonArray("planets"));
                }

                if (visual.has("moons") && visual.get("moons").isJsonArray()) {
                    parsePlanetsArray(visual.getAsJsonArray("moons"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parsePlanetsArray(JsonArray arr) {
        for (JsonElement el : arr) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            parsePlanetConfig(obj);
        }
    }

    /**
     * Tolerant parser:
     * - supports flat keys (forestFiresEnabled, minDuration, skyTintStart etc.)
     * - supports nested objects (forestFires.enabled, skyColors.sunriseColor)
     */
    private static void parsePlanetConfig(JsonObject planetObj) {
        if (!planetObj.has("name")) return;
        String name = planetObj.get("name").getAsString().trim();
        if (name.isEmpty()) return;
        String key = name.toLowerCase();

        // ---------- Forest fire values (support flat or nested)
        boolean enabled = false;
        double minDur = 2.0, maxDur = 10.0, minInt = 30.0, maxInt = 120.0, maxTint = 0.7, spreadRadius = 5000.0;
        int tintStart = 0xFFAA00, tintEnd = 0xFF4400;

        // flat fields
        if (planetObj.has("forestFiresEnabled")) enabled = safeGetBoolean(planetObj, "forestFiresEnabled", false);
        if (planetObj.has("minDuration")) minDur = safeGetDouble(planetObj, "minDuration", minDur);
        if (planetObj.has("maxDuration")) maxDur = safeGetDouble(planetObj, "maxDuration", maxDur);
        if (planetObj.has("minInterval")) minInt = safeGetDouble(planetObj, "minInterval", minInt);
        if (planetObj.has("maxInterval")) maxInt = safeGetDouble(planetObj, "maxInterval", maxInt);
        if (planetObj.has("maxTintStrength")) maxTint = safeGetDouble(planetObj, "maxTintStrength", maxTint);
        if (planetObj.has("spreadRadius")) spreadRadius = safeGetDouble(planetObj, "spreadRadius", spreadRadius);

        if (planetObj.has("skyTintStart")) tintStart = parseColor(planetObj, "skyTintStart", tintStart);
        if (planetObj.has("skyTintEnd")) tintEnd = parseColor(planetObj, "skyTintEnd", tintEnd);

        // nested forestFires object support
        if (planetObj.has("forestFires") && planetObj.get("forestFires").isJsonObject()) {
            JsonObject ff = planetObj.getAsJsonObject("forestFires");
            enabled = ff.has("enabled") ? safeGetBoolean(ff, "enabled", enabled) : enabled;
            minDur = ff.has("minDuration") ? safeGetDouble(ff, "minDuration", minDur) : minDur;
            maxDur = ff.has("maxDuration") ? safeGetDouble(ff, "maxDuration", maxDur) : maxDur;
            minInt = ff.has("minInterval") ? safeGetDouble(ff, "minInterval", minInt) : minInt;
            maxInt = ff.has("maxInterval") ? safeGetDouble(ff, "maxInterval", maxInt) : maxInt;
            maxTint = ff.has("maxTintStrength") ? safeGetDouble(ff, "maxTintStrength", maxTint) : maxTint;
            spreadRadius = ff.has("spreadRadius") ? safeGetDouble(ff, "spreadRadius", spreadRadius) : spreadRadius;
            tintStart = ff.has("skyTintStart") ? parseColor(ff, "skyTintStart", tintStart) : tintStart;
            tintEnd = ff.has("skyTintEnd") ? parseColor(ff, "skyTintEnd", tintEnd) : tintEnd;
        }

        ForestFireConfig fcfg = new ForestFireConfig(enabled, minDur, maxDur, minInt, maxInt, tintStart, tintEnd, maxTint, spreadRadius);

        // ---------- Sky colors (flat keys or nested skyColors)
        int sunrise = parseColor(planetObj, "sunriseColor", 0xFFA040);
        int sunset  = parseColor(planetObj, "sunsetColor", 0xFF6020);
        int day     = parseColor(planetObj, "dayColor", 0x87CEEB);
        int night   = parseColor(planetObj, "nightColor", 0x000814);
        int horizon = parseColor(planetObj, "horizonColor", 0xFFB080);

        if (planetObj.has("skyColors") && planetObj.get("skyColors").isJsonObject()) {
            JsonObject sc = planetObj.getAsJsonObject("skyColors");
            sunrise = sc.has("sunriseColor") ? parseColor(sc, "sunriseColor", sunrise) : sunrise;
            sunset  = sc.has("sunsetColor")  ? parseColor(sc, "sunsetColor", sunset) : sunset;
            day     = sc.has("dayColor")     ? parseColor(sc, "dayColor", day) : day;
            night   = sc.has("nightColor")   ? parseColor(sc, "nightColor", night) : night;
            horizon = sc.has("horizonColor") ? parseColor(sc, "horizonColor", horizon) : horizon;
        }

        SkyColorsConfig sccfg = new SkyColorsConfig(sunrise, sunset, day, night, horizon);

        ATMOSPHERE_MAP.put(key, new PlanetAtmosphere(fcfg, sccfg));
    }

    // ---------- Utilities ----------
    private static boolean safeGetBoolean(JsonObject o, String key, boolean def) {
        try {
            if (!o.has(key)) return def;
            JsonElement e = o.get(key);
            if (e.isJsonPrimitive()) {
                JsonPrimitive p = e.getAsJsonPrimitive();
                if (p.isBoolean()) return p.getAsBoolean();
                if (p.isString()) return Boolean.parseBoolean(p.getAsString());
                if (p.isNumber()) return p.getAsInt() != 0;
            }
        } catch (Exception ignored) {}
        return def;
    }

    private static double safeGetDouble(JsonObject o, String key, double def) {
        try {
            if (!o.has(key)) return def;
            JsonElement e = o.get(key);
            if (e.isJsonPrimitive()) {
                JsonPrimitive p = e.getAsJsonPrimitive();
                if (p.isNumber()) return p.getAsDouble();
                if (p.isString()) {
                    String s = p.getAsString().trim();
                    if (s.isEmpty()) return def;
                    return Double.parseDouble(s);
                }
            }
        } catch (Exception ignored) {}
        return def;
    }

    private static int parseColor(JsonObject o, String key, int defaultValue) {
        try {
            if (!o.has(key)) return defaultValue;
            String s = o.get(key).getAsString().trim();
            if (s.startsWith("#")) s = s.substring(1);
            if (s.length() == 6 || s.length() == 8) {
                return (int)Long.parseLong(s, 16);
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }

    // ---------- Accessors & simulation ----------
    public static PlanetAtmosphere getAtmosphere(String planetName) {
        if (ATMOSPHERE_MAP == null) load();
        if (planetName == null) return null;
        return ATMOSPHERE_MAP.get(planetName.toLowerCase());
    }

    public static Set<String> getLoadedBodies() {
        if (ATMOSPHERE_MAP == null) load();
        return ATMOSPHERE_MAP.keySet();
    }

    public static void reload() {
        load();
    }

    public static void dump() {
        if (ATMOSPHERE_MAP == null) load();
    }

    public static void updateForestFires(String planetName, double currentTime, double playerX, double playerZ) {
        PlanetAtmosphere atmosphere = getAtmosphere(planetName);
        if (atmosphere == null || atmosphere.forestFires == null || !atmosphere.forestFires.enabled) {
            return;
        }

        ForestFireState state = FIRE_STATES.computeIfAbsent(planetName.toLowerCase(), k -> new ForestFireState());

        if (state.nextFireTime == 0) {
            double interval = atmosphere.forestFires.minInterval +
                    random.nextDouble() * (atmosphere.forestFires.maxInterval - atmosphere.forestFires.minInterval);
            state.nextFireTime = currentTime + interval;
        }

        if (!state.active && currentTime >= state.nextFireTime) {
            state.active = true;
            state.startTime = currentTime;
            double duration = atmosphere.forestFires.minDuration +
                    random.nextDouble() * (atmosphere.forestFires.maxDuration - atmosphere.forestFires.minDuration);
            state.endTime = currentTime + duration;

            state.centerX = playerX + (random.nextDouble() - 0.5) * 10000.0;
            state.centerZ = playerZ + (random.nextDouble() - 0.5) * 10000.0;
            state.intensity = 1.0;

        }

        if (state.active && currentTime >= state.endTime) {
            state.active = false;
            double interval = atmosphere.forestFires.minInterval +
                    random.nextDouble() * (atmosphere.forestFires.maxInterval - atmosphere.forestFires.minInterval);
            state.nextFireTime = currentTime + interval;
        }
    }

    public static boolean isForestFireActive(String planetName) {
        ForestFireState state = FIRE_STATES.get(planetName == null ? null : planetName.toLowerCase());
        return state != null && state.active;
    }

    public static ForestFireState getForestFireState(String planetName) {
        return FIRE_STATES.get(planetName == null ? null : planetName.toLowerCase());
    }
}
