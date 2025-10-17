package net.witherstorm8475.astrocraftaddon.atmospheric;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AtmosphericEvents {

    private static Map<String, PlanetAtmosphere> ATMOSPHERE_MAP = null;
    private static Map<String, ForestFireState> FIRE_STATES = new HashMap<>();
    private static Random random = new Random();

    public static class PlanetAtmosphere {
        public final ForestFireConfig forestFires;
        public final SkyColorsConfig skyColors;

        public PlanetAtmosphere(ForestFireConfig fires, SkyColorsConfig colors) {
            this.forestFires = fires;
            this.skyColors = colors;
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
    }

    public static class ForestFireState {
        public boolean active;
        public double startTime;
        public double endTime;
        public double nextFireTime;
        public double intensity;
        public double centerX;
        public double centerZ;

        public ForestFireState() {
            this.active = false;
            this.startTime = 0;
            this.endTime = 0;
            this.nextFireTime = 0;
            this.intensity = 0;
            this.centerX = 0;
            this.centerZ = 0;
        }
    }

    public static class SkyColorsConfig {
        public final int sunriseColor;
        public final int sunsetColor;
        public final int dayColor;
        public final int nightColor;
        public final int horizonColor;

        public SkyColorsConfig(int sunrise, int sunset, int day, int night, int horizon) {
            this.sunriseColor = sunrise;
            this.sunsetColor = sunset;
            this.dayColor = day;
            this.nightColor = night;
            this.horizonColor = horizon;
        }
    }

    public static void load() {
        ATMOSPHERE_MAP = new HashMap<>();

        try {
            InputStream stream = null;
            stream = AtmosphericEvents.class.getResourceAsStream("/astrocraft-151-addon/astrocraft-addon.json");
            if (stream == null) {
                stream = AtmosphericEvents.class.getResourceAsStream("astrocraft-151-addon/astrocraft-addon.json");
            }
            if (stream == null) {
                stream = AtmosphericEvents.class.getClassLoader().getResourceAsStream("config/astrocraft-addon.json");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
            parseJson(jsonContent.toString());
            System.out.println("Loaded atmospheric events for " + ATMOSPHERE_MAP.size() + " planets");
        } catch (Exception e) {
            System.err.println("Error loading astrocraft-addon.json:");
            e.printStackTrace();
        }
    }

    private static void parseJson(String json) {
        json = json.trim();

        int visualStart = json.indexOf("\"visual\"");
        int visualEnd = findMatchingBrace(json, json.indexOf("{", visualStart));

        String visualSection = json.substring(visualStart, visualEnd);

        // Parse planets array - search within visualSection
        int planetsStart = visualSection.indexOf("\"planets\"");
        if (planetsStart != -1) {
            planetsStart = visualSection.indexOf("[", planetsStart);  // Search in visualSection, not json
            int planetsEnd = findMatchingBracket(visualSection, planetsStart);  // Use visualSection
            if (planetsStart != -1 && planetsEnd != -1) {
                String planetsArray = visualSection.substring(planetsStart + 1, planetsEnd);
                parsePlanetsArray(planetsArray);
            }
        }

        // Parse moons array - search within visualSection
        int moonsStart = visualSection.indexOf("\"moons\"");
        if (moonsStart != -1) {
            moonsStart = visualSection.indexOf("[", moonsStart);  // Search in visualSection, not json
            int moonsEnd = findMatchingBracket(visualSection, moonsStart);  // Use visualSection
            if (moonsStart != -1 && moonsEnd != -1) {
                String moonsArray = visualSection.substring(moonsStart + 1, moonsEnd);
                parsePlanetsArray(moonsArray);
            }
        }
    }

    private static int findMatchingBrace(String json, int openIndex) {
        if (openIndex == -1 || openIndex >= json.length()) return -1;
        int depth = 0;
        for (int i = openIndex; i < json.length(); i++) {
            if (json.charAt(i) == '{') depth++;
            else if (json.charAt(i) == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static int findMatchingBracket(String json, int openIndex) {
        if (openIndex == -1 || openIndex >= json.length()) return -1;
        int depth = 0;
        for (int i = openIndex; i < json.length(); i++) {
            if (json.charAt(i) == '[') depth++;
            else if (json.charAt(i) == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static void parsePlanetsArray(String arrayContent) {
        String[] planetObjects = arrayContent.split("\\},\\s*\\{");
        for (String obj : planetObjects) {
            obj = obj.replace("{", "").replace("}", "").trim();
            parsePlanetConfig(obj);
        }
    }

    private static void parsePlanetConfig(String planetObj) {
        String name = extractValue(planetObj, "name");
        if (name == null) return;

        ForestFireConfig fireConfig = parseForestFireConfig(planetObj);
        SkyColorsConfig colorsConfig = parseSkyColorsConfig(planetObj);

        if (fireConfig != null || colorsConfig != null) {
            ATMOSPHERE_MAP.put(name.toLowerCase(), new PlanetAtmosphere(fireConfig, colorsConfig));
        }
    }

    private static ForestFireConfig parseForestFireConfig(String planetObj) {
        int fireStart = planetObj.indexOf("\"forestFires\"");
        if (fireStart == -1) return null;

        String fireSection = planetObj.substring(fireStart);
        boolean enabled = fireSection.contains("\"enabled\": true");
        double minDur = parseDouble(fireSection, "minDuration", 2.0);
        double maxDur = parseDouble(fireSection, "maxDuration", 10.0);
        double minInt = parseDouble(fireSection, "minInterval", 30.0);
        double maxInt = parseDouble(fireSection, "maxInterval", 120.0);
        double maxTint = parseDouble(fireSection, "maxTintStrength", 0.7);
        double radius = parseDouble(fireSection, "spreadRadius", 5000.0);

        int tintStart = parseColor(fireSection, "skyTintStart", 0xFFAA00);
        int tintEnd = parseColor(fireSection, "skyTintEnd", 0xFF4400);

        return new ForestFireConfig(enabled, minDur, maxDur, minInt, maxInt, tintStart, tintEnd, maxTint, radius);
    }

    private static int parseColor(String json, String key, int defaultValue) {
        String colorStr = extractValue(json, key);
        if (colorStr != null && colorStr.startsWith("#")) {
            try {
                return Integer.parseInt(colorStr.substring(1), 16);
            } catch (Exception e) {}
        }
        return defaultValue;
    }

    private static SkyColorsConfig parseSkyColorsConfig(String planetObj) {
        int colorsStart = planetObj.indexOf("\"skyColors\"");
        if (colorsStart == -1) return null;

        String colorsSection = planetObj.substring(colorsStart);
        int sunrise = parseColor(colorsSection, "sunriseColor", 0xFFA040);
        int sunset = parseColor(colorsSection, "sunsetColor", 0xFF6020);
        int day = parseColor(colorsSection, "dayColor", 0x87CEEB);
        int night = parseColor(colorsSection, "nightColor", 0x000814);
        int horizon = parseColor(colorsSection, "horizonColor", 0xFFB080);

        return new SkyColorsConfig(sunrise, sunset, day, night, horizon);
    }

    private static String extractValue(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return null;
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) return null;

        if (json.charAt(valueStart) == '"') {
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1) return null;
            return json.substring(valueStart + 1, valueEnd);
        }
        return null;
    }

    private static double parseDouble(String json, String key, double defaultValue) {
        int keyIndex = json.indexOf("\"" + key.split("\\*")[key.split("\\*").length - 1] + "\"");
        if (keyIndex == -1) return defaultValue;
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return defaultValue;

        StringBuilder valueStr = new StringBuilder();
        int i = colonIndex + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        while (i < json.length() && (Character.isDigit(json.charAt(i)) || json.charAt(i) == '.' || json.charAt(i) == '-')) {
            valueStr.append(json.charAt(i));
            i++;
        }

        try {
            return Double.parseDouble(valueStr.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static PlanetAtmosphere getAtmosphere(String planetName) {
        if (ATMOSPHERE_MAP == null) {
            load();
        }
        return ATMOSPHERE_MAP.get(planetName.toLowerCase());
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

            System.out.println("Forest fire started on " + planetName + " for " + duration + " days at " + state.centerX + ", " + state.centerZ);
        }

        if (state.active && currentTime >= state.endTime) {
            state.active = false;
            double interval = atmosphere.forestFires.minInterval +
                    random.nextDouble() * (atmosphere.forestFires.maxInterval - atmosphere.forestFires.minInterval);
            state.nextFireTime = currentTime + interval;
            System.out.println("Forest fire ended on " + planetName + ". Next in " + interval + " days");
        }
    }

    public static boolean isForestFireActive(String planetName) {
        ForestFireState state = FIRE_STATES.get(planetName.toLowerCase());
        return state != null && state.active;
    }

    public static ForestFireState getForestFireState(String planetName) {
        return FIRE_STATES.get(planetName.toLowerCase());
    }
}