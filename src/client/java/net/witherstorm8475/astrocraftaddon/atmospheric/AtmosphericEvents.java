package net.witherstorm8475.astrocraftaddon.atmospheric;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AtmosphericEvents {

    private static Map<String, PlanetAtmosphere> ATMOSPHERE_MAP = null;
    private static Map<String, SolarStormState> STORM_STATES = new HashMap<>();
    private static Map<String, ForestFireState> FIRE_STATES = new HashMap<>();
    private static LightPollutionConfig GLOBAL_LIGHT_POLLUTION = null;
    private static Random random = new Random();

    public static class LightPollutionConfig {
        public final LightPollutionSystem.Mode mode;
        public final float intensity;

        public LightPollutionConfig(String modeStr, float intensity) {
            LightPollutionSystem.Mode parsedMode;
            try {
                parsedMode = LightPollutionSystem.Mode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                parsedMode = LightPollutionSystem.Mode.OFF;
            }
            this.mode = parsedMode;
            this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
        }
    }

    public static class PlanetAtmosphere {
        public final AuroraConfig auroras;
        public final SolarStormConfig solarStorms;
        public final ForestFireConfig forestFires;
        public final SkyColorsConfig skyColors;

        public PlanetAtmosphere(AuroraConfig auroras, SolarStormConfig storms, ForestFireConfig fires, SkyColorsConfig colors) {
            this.auroras = auroras;
            this.solarStorms = storms;
            this.forestFires = fires;
            this.skyColors = colors;
        }
    }

    public static class AuroraConfig {
        public final boolean enabled;
        public final int[] colors;
        public final double borealisMinLat;
        public final double borealisMaxLat;
        public final double australisMinLat;
        public final double australisMaxLat;
        public final double height;
        public final double thickness;
        public final double waveSpeed;
        public final double waveAmplitude;
        public final double horizontalFlowSpeed;

        public AuroraConfig(boolean enabled, int[] colors,
                            double bMinLat, double bMaxLat,
                            double aMinLat, double aMaxLat,
                            double height, double thickness,
                            double waveSpeed, double waveAmp, double flowSpeed) {
            this.enabled = enabled;
            this.colors = colors;
            this.borealisMinLat = bMinLat;
            this.borealisMaxLat = bMaxLat;
            this.australisMinLat = aMinLat;
            this.australisMaxLat = aMaxLat;
            this.height = height;
            this.thickness = thickness;
            this.waveSpeed = waveSpeed;
            this.waveAmplitude = waveAmp;
            this.horizontalFlowSpeed = flowSpeed;
        }
    }

    public static class SolarStormConfig {
        public final boolean enabled;
        public final double minDuration;
        public final double maxDuration;
        public final double minInterval;
        public final double maxInterval;
        public final double intensity;

        public SolarStormConfig(boolean enabled, double minDur, double maxDur,
                                double minInt, double maxInt, double intensity) {
            this.enabled = enabled;
            this.minDuration = minDur;
            this.maxDuration = maxDur;
            this.minInterval = minInt;
            this.maxInterval = maxInt;
            this.intensity = intensity;
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

    public static class SolarStormState {
        public boolean active;
        public double startTime;
        public double endTime;
        public double nextStormTime;
        public double intensity;

        public SolarStormState() {
            this.active = false;
            this.startTime = 0;
            this.endTime = 0;
            this.nextStormTime = 0;
            this.intensity = 0;
        }
    }

    public static void load() {
        ATMOSPHERE_MAP = new HashMap<>();
        GLOBAL_LIGHT_POLLUTION = null;

        try {
            InputStream stream = null;
            stream = AtmosphericEvents.class.getResourceAsStream("/astrocraft-151-addon/atmosphericevents.json");
            if (stream == null) {
                stream = AtmosphericEvents.class.getResourceAsStream("astrocraft-151-addon/atmosphericevents.json");
            }
            if (stream == null) {
                stream = AtmosphericEvents.class.getClassLoader().getResourceAsStream("astrocraft-151-addon/atmosphericevents.json");
            }
            if (stream == null) {
                File configFile = new File("config/astrocraft-151-addon/atmosphericevents.json");
                if (configFile.exists()) {
                    stream = new FileInputStream(configFile);
                    System.out.println("Loading atmosphericevents.json from config folder");
                }
            }
            if (stream == null) {
                System.out.println("atmosphericevents.json not found, generating default...");
                generateDefaultConfig();
                return;
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
            if (GLOBAL_LIGHT_POLLUTION != null) {
                System.out.println("Light pollution mode: " + GLOBAL_LIGHT_POLLUTION.mode + " (intensity: " + GLOBAL_LIGHT_POLLUTION.intensity + ")");
            }
        } catch (Exception e) {
            System.err.println("Error loading atmosphericevents.json:");
            e.printStackTrace();
        }
    }

    private static void generateDefaultConfig() {
        try {
            StringBuilder json = new StringBuilder();

            json.append("{\n");
            json.append("  \"lightPollution\": {\n");
            json.append("    \"mode\": \"dynamic\",\n");
            json.append("    \"intensity\": 1\n");
            json.append("  },\n");

            json.append("  \"planets\": [\n");

            // Venus
            json.append("    {\n");
            json.append("      \"name\": \"Venus\",\n");
            json.append("      \"auroras\": { \"enabled\": false, \"colors\": [], \"borealis\": {\"minLatitude\":0.0,\"maxLatitude\":0.0}, \"australis\": {\"minLatitude\":0.0,\"maxLatitude\":0.0}, \"height\":0.0,\"thickness\":0.0,\"waveSpeed\":0.0,\"waveAmplitude\":0.0,\"horizontalFlowSpeed\":0.0 },\n");
            json.append("      \"solarStorms\": { \"enabled\": false, \"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"intensity\":0.0 },\n");
            json.append("      \"forestFires\": { \"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0 },\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#FFDAB9\",\"sunsetColor\":\"#FFCC99\",\"dayColor\":\"#FFE4B5\",\"nightColor\":\"#2F1E0F\",\"horizonColor\":\"#FFC87C\"}\n");
            json.append("    },\n");

            // Earth
            json.append("    {\n");
            json.append("      \"name\": \"Earth\",\n");
            json.append("      \"auroras\": { \"enabled\": true, \"colors\": [\"#00FF00\",\"#00FF88\",\"#0088FF\"], \"borealis\": {\"minLatitude\":-80.0,\"maxLatitude\":-60.0}, \"australis\": {\"minLatitude\":60.0,\"maxLatitude\":80.0}, \"height\":150.0,\"thickness\":30.0,\"waveSpeed\":0.05,\"waveAmplitude\":15.0,\"horizontalFlowSpeed\":0.02 },\n");
            json.append("      \"solarStorms\": { \"enabled\": true, \"minDuration\":1.0,\"maxDuration\":5.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"intensity\":0.8 },\n");
            json.append("      \"forestFires\": { \"enabled\": true, \"minDuration\":2.0,\"maxDuration\":10.0,\"minInterval\":30.0,\"maxInterval\":120.0,\"skyTintStart\":\"#FFAA00\",\"skyTintEnd\":\"#FF4400\",\"maxTintStrength\":0.7,\"spreadRadius\":5000.0 },\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#FFA040\",\"sunsetColor\":\"#FF6020\",\"dayColor\":\"#87CEEB\",\"nightColor\":\"#000814\",\"horizonColor\":\"#FFB080\"}\n");
            json.append("    },\n");

            // Mars
            json.append("    {\n");
            json.append("      \"name\": \"Mars\",\n");
            json.append("      \"auroras\": {\"enabled\": false,\"colors\":[],\"borealis\":{\"minLatitude\":0.0,\"maxLatitude\":0.0},\"australis\":{\"minLatitude\":0.0,\"maxLatitude\":0.0},\"height\":0.0,\"thickness\":0.0,\"waveSpeed\":0.0,\"waveAmplitude\":0.0,\"horizontalFlowSpeed\":0.0},\n");
            json.append("      \"solarStorms\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"intensity\":0.0},\n");
            json.append("      \"forestFires\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0},\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#4080C0\",\"sunsetColor\":\"#2060A0\",\"dayColor\":\"#E8C9A6\",\"nightColor\":\"#1A1410\",\"horizonColor\":\"#C09060\"}\n");
            json.append("    },\n");

            // Jupiter
            json.append("    {\n");
            json.append("      \"name\": \"Jupiter\",\n");
            json.append("      \"auroras\": { \"enabled\": true, \"colors\": [\"#8800FF\",\"#FF00FF\",\"#FF0088\"], \"borealis\": {\"minLatitude\":-75.0,\"maxLatitude\":-55.0}, \"australis\": {\"minLatitude\":55.0,\"maxLatitude\":75.0}, \"height\":200.0,\"thickness\":50.0,\"waveSpeed\":0.1,\"waveAmplitude\":25.0,\"horizontalFlowSpeed\":0.05 },\n");
            json.append("      \"solarStorms\": { \"enabled\": true, \"minDuration\":5.0,\"maxDuration\":15.0,\"minInterval\":10.0,\"maxInterval\":30.0,\"intensity\":1.0 },\n");
            json.append("      \"forestFires\": { \"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0 },\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#FFD8FF\",\"sunsetColor\":\"#FFA0FF\",\"dayColor\":\"#FFCCFF\",\"nightColor\":\"#1A001A\",\"horizonColor\":\"#FFB0FF\"}\n");
            json.append("    },\n");

            // Saturn
            json.append("    {\n");
            json.append("      \"name\": \"Saturn\",\n");
            json.append("      \"auroras\": { \"enabled\": true, \"colors\": [\"#0088FF\",\"#00FFFF\"], \"borealis\": {\"minLatitude\":-85.0,\"maxLatitude\":-70.0}, \"australis\": {\"minLatitude\":70.0,\"maxLatitude\":85.0}, \"height\":180.0,\"thickness\":40.0,\"waveSpeed\":0.08,\"waveAmplitude\":20.0,\"horizontalFlowSpeed\":0.03 },\n");
            json.append("      \"solarStorms\": { \"enabled\": true, \"minDuration\":3.0,\"maxDuration\":10.0,\"minInterval\":30.0,\"maxInterval\":80.0,\"intensity\":0.7 },\n");
            json.append("      \"forestFires\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0 },\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#D0E8FF\",\"sunsetColor\":\"#A0C8FF\",\"dayColor\":\"#C0E0FF\",\"nightColor\":\"#0A0010\",\"horizonColor\":\"#B0D0FF\"}\n");
            json.append("    },\n");

            // Uranus
            json.append("    {\n");
            json.append("      \"name\": \"Uranus\",\n");
            json.append("      \"auroras\": { \"enabled\": true, \"colors\": [\"#00FFFF\",\"#88FFFF\"], \"borealis\": {\"minLatitude\":-70.0,\"maxLatitude\":-50.0}, \"australis\": {\"minLatitude\":50.0,\"maxLatitude\":70.0}, \"height\":170.0,\"thickness\":35.0,\"waveSpeed\":0.06,\"waveAmplitude\":18.0,\"horizontalFlowSpeed\":0.025 },\n");
            json.append("      \"solarStorms\": { \"enabled\": true, \"minDuration\":2.0,\"maxDuration\":8.0,\"minInterval\":40.0,\"maxInterval\":100.0,\"intensity\":0.6 },\n");
            json.append("      \"forestFires\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0 },\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#A0FFFF\",\"sunsetColor\":\"#80FFFF\",\"dayColor\":\"#B0FFFF\",\"nightColor\":\"#001020\",\"horizonColor\":\"#90FFFF\"}\n");
            json.append("    },\n");

            // Neptune
            json.append("    {\n");
            json.append("      \"name\": \"Neptune\",\n");
            json.append("      \"auroras\": { \"enabled\": true, \"colors\": [\"#0044FF\",\"#0088FF\"], \"borealis\": {\"minLatitude\":-80.0,\"maxLatitude\":-65.0}, \"australis\": {\"minLatitude\":65.0,\"maxLatitude\":80.0}, \"height\":160.0,\"thickness\":35.0,\"waveSpeed\":0.07,\"waveAmplitude\":20.0,\"horizontalFlowSpeed\":0.03 },\n");
            json.append("      \"solarStorms\": { \"enabled\": true, \"minDuration\":2.0,\"maxDuration\":7.0,\"minInterval\":50.0,\"maxInterval\":120.0,\"intensity\":0.5 },\n");
            json.append("      \"forestFires\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0 },\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#809FFF\",\"sunsetColor\":\"#4060FF\",\"dayColor\":\"#6090FF\",\"nightColor\":\"#000810\",\"horizonColor\":\"#5070FF\"}\n");
            json.append("    }\n");

            json.append("  ],\n");

            json.append("  \"moons\": [\n");
            json.append("    {\n");
            json.append("      \"name\": \"Titan\",\n");
            json.append("      \"auroras\": { \"enabled\": false,\"colors\":[],\"borealis\":{\"minLatitude\":0.0,\"maxLatitude\":0.0},\"australis\":{\"minLatitude\":0.0,\"maxLatitude\":0.0},\"height\":0.0,\"thickness\":0.0,\"waveSpeed\":0.0,\"waveAmplitude\":0.0,\"horizontalFlowSpeed\":0.0},\n");
            json.append("      \"solarStorms\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"intensity\":0.0},\n");
            json.append("      \"forestFires\": {\"enabled\": false,\"minDuration\":0.0,\"maxDuration\":0.0,\"minInterval\":0.0,\"maxInterval\":0.0,\"skyTintStart\":\"#000000\",\"skyTintEnd\":\"#000000\",\"maxTintStrength\":0.0,\"spreadRadius\":0.0},\n");
            json.append("      \"skyColors\": {\"sunriseColor\":\"#FFDDAA\",\"sunsetColor\":\"#FFCC88\",\"dayColor\":\"#FFE8AA\",\"nightColor\":\"#1A0F0F\",\"horizonColor\":\"#FFCC88\"}\n");
            json.append("    }\n");
            json.append("  ]\n");
            json.append("}\n");

            File configDir = new File("config/astrocraft-151-addon");
            if (!configDir.exists()) configDir.mkdirs();
            File jsonFile = new File(configDir, "atmosphericevents.json");
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(json.toString());
            writer.close();

            System.out.println("Generated atmosphericevents.json at: " + jsonFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error generating atmosphericevents.json:");
            e.printStackTrace();
        }
    }

    private static void parseJson(String json) {
        json = json.trim();

        // Parse global light pollution
        int lpStart = json.indexOf("\"lightPollution\"");
        if (lpStart != -1) {
            lpStart = json.indexOf("{", lpStart);
            int lpEnd = findMatchingBrace(json, lpStart);
            if (lpStart != -1 && lpEnd != -1) {
                String lpObject = json.substring(lpStart + 1, lpEnd);
                String mode = extractValue(lpObject, "mode");
                if (mode == null) mode = "OFF";
                float intensity = (float) parseDouble(lpObject, "intensity", 0.5);
                GLOBAL_LIGHT_POLLUTION = new LightPollutionConfig(mode, intensity);
            }
        }

        // Parse planets array
        int planetsStart = json.indexOf("\"planets\"");
        if (planetsStart != -1) {
            planetsStart = json.indexOf("[", planetsStart);
            int planetsEnd = findMatchingBracket(json, planetsStart);
            if (planetsStart != -1 && planetsEnd != -1) {
                String planetsArray = json.substring(planetsStart + 1, planetsEnd);
                parsePlanetsArray(planetsArray);
            }
        }

        // Parse moons array
        int moonsStart = json.indexOf("\"moons\"");
        if (moonsStart != -1) {
            moonsStart = json.indexOf("[", moonsStart);
            int moonsEnd = findMatchingBracket(json, moonsStart);
            if (moonsStart != -1 && moonsEnd != -1) {
                String moonsArray = json.substring(moonsStart + 1, moonsEnd);
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

        AuroraConfig auroraConfig = parseAuroraConfig(planetObj);
        SolarStormConfig stormConfig = parseSolarStormConfig(planetObj);
        ForestFireConfig fireConfig = parseForestFireConfig(planetObj);
        SkyColorsConfig colorsConfig = parseSkyColorsConfig(planetObj);

        if (auroraConfig != null || stormConfig != null || fireConfig != null || colorsConfig != null) {
            ATMOSPHERE_MAP.put(name.toLowerCase(), new PlanetAtmosphere(auroraConfig, stormConfig, fireConfig, colorsConfig));
        }
    }

    private static AuroraConfig parseAuroraConfig(String planetObj) {
        int auroraStart = planetObj.indexOf("\"auroras\"");
        if (auroraStart == -1) return null;

        boolean enabled = planetObj.contains("\"enabled\": true");

        List<Integer> colorList = new ArrayList<>();
        int colorsStart = planetObj.indexOf("\"colors\"", auroraStart);
        if (colorsStart != -1) {
            int arrayStart = planetObj.indexOf("[", colorsStart);
            int arrayEnd = planetObj.indexOf("]", arrayStart);
            if (arrayStart != -1 && arrayEnd != -1) {
                String colorsStr = planetObj.substring(arrayStart + 1, arrayEnd);
                String[] colorStrs = colorsStr.split(",");
                for (String colorStr : colorStrs) {
                    colorStr = colorStr.trim().replace("\"", "");
                    if (colorStr.startsWith("#")) {
                        try {
                            colorList.add(Integer.parseInt(colorStr.substring(1), 16));
                        } catch (Exception e) {}
                    }
                }
            }
        }

        int[] colors = colorList.stream().mapToInt(i -> i).toArray();

        double bMinLat = parseDouble(planetObj, "borealis.*minLatitude", 60.0);
        double bMaxLat = parseDouble(planetObj, "borealis.*maxLatitude", 80.0);
        double aMinLat = parseDouble(planetObj, "australis.*minLatitude", -80.0);
        double aMaxLat = parseDouble(planetObj, "australis.*maxLatitude", -60.0);

        double height = parseDouble(planetObj, "height", 150.0);
        double thickness = parseDouble(planetObj, "thickness", 30.0);
        double waveSpeed = parseDouble(planetObj, "waveSpeed", 0.05);
        double waveAmp = parseDouble(planetObj, "waveAmplitude", 15.0);
        double flowSpeed = parseDouble(planetObj, "horizontalFlowSpeed", 0.02);

        return new AuroraConfig(enabled, colors, bMinLat, bMaxLat, aMinLat, aMaxLat,
                height, thickness, waveSpeed, waveAmp, flowSpeed);
    }

    private static SolarStormConfig parseSolarStormConfig(String planetObj) {
        int stormStart = planetObj.indexOf("\"solarStorms\"");
        if (stormStart == -1) return null;

        boolean enabled = planetObj.substring(stormStart).contains("\"enabled\": true");
        double minDur = parseDouble(planetObj.substring(stormStart), "minDuration", 1.0);
        double maxDur = parseDouble(planetObj.substring(stormStart), "maxDuration", 5.0);
        double minInt = parseDouble(planetObj.substring(stormStart), "minInterval", 20.0);
        double maxInt = parseDouble(planetObj.substring(stormStart), "maxInterval", 50.0);
        double intensity = parseDouble(planetObj.substring(stormStart), "intensity", 0.8);

        return new SolarStormConfig(enabled, minDur, maxDur, minInt, maxInt, intensity);
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

    public static LightPollutionConfig getLightPollution() {
        if (GLOBAL_LIGHT_POLLUTION == null) {
            load();
        }
        return GLOBAL_LIGHT_POLLUTION;
    }

    public static void updateSolarStorms(String planetName, double currentTime) {
        PlanetAtmosphere atmosphere = getAtmosphere(planetName);
        if (atmosphere == null || atmosphere.solarStorms == null || !atmosphere.solarStorms.enabled) {
            return;
        }

        SolarStormState state = STORM_STATES.computeIfAbsent(planetName.toLowerCase(), k -> new SolarStormState());

        if (state.nextStormTime == 0) {
            double interval = atmosphere.solarStorms.minInterval +
                    random.nextDouble() * (atmosphere.solarStorms.maxInterval - atmosphere.solarStorms.minInterval);
            state.nextStormTime = currentTime + interval;
        }

        if (!state.active && currentTime >= state.nextStormTime) {
            state.active = true;
            state.startTime = currentTime;
            double duration = atmosphere.solarStorms.minDuration +
                    random.nextDouble() * (atmosphere.solarStorms.maxDuration - atmosphere.solarStorms.minDuration);
            state.endTime = currentTime + duration;
            state.intensity = atmosphere.solarStorms.intensity;
            System.out.println("Solar storm started on " + planetName + " for " + duration + " days");
        }

        if (state.active && currentTime >= state.endTime) {
            state.active = false;
            double interval = atmosphere.solarStorms.minInterval +
                    random.nextDouble() * (atmosphere.solarStorms.maxInterval - atmosphere.solarStorms.minInterval);
            state.nextStormTime = currentTime + interval;
            System.out.println("Solar storm ended on " + planetName + ". Next in " + interval + " days");
        }
    }

    public static boolean isSolarStormActive(String planetName) {
        SolarStormState state = STORM_STATES.get(planetName.toLowerCase());
        return state != null && state.active;
    }

    public static double getSolarStormIntensity(String planetName) {
        SolarStormState state = STORM_STATES.get(planetName.toLowerCase());
        return (state != null && state.active) ? state.intensity : 0.0;
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