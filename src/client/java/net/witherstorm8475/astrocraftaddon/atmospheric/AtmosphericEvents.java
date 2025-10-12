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
    private static Random random = new Random();

    public static class PlanetAtmosphere {
        public final AuroraConfig auroras;
        public final SolarStormConfig solarStorms;

        public PlanetAtmosphere(AuroraConfig auroras, SolarStormConfig storms) {
            this.auroras = auroras;
            this.solarStorms = storms;
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
        } catch (Exception e) {
            System.err.println("Error loading atmosphericevents.json:");
            e.printStackTrace();
        }
    }

    private static void generateDefaultConfig() {
        // Generate default atmosphericevents.json with all planets and moons
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n  \"planets\": [\n");

            // Earth
            json.append("    {\n");
            json.append("      \"name\": \"Earth\",\n");
            json.append("      \"auroras\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"colors\": [\"#00FF00\", \"#00FF88\", \"#0088FF\"],\n");
            json.append("        \"borealis\": {\"minLatitude\": -80.0, \"maxLatitude\": -60.0},\n");
            json.append("        \"australis\": {\"minLatitude\": 60.0, \"maxLatitude\": 80.0},\n");
            json.append("        \"height\": 150.0,\n");
            json.append("        \"thickness\": 30.0,\n");
            json.append("        \"waveSpeed\": 0.05,\n");
            json.append("        \"waveAmplitude\": 15.0,\n");
            json.append("        \"horizontalFlowSpeed\": 0.02\n");
            json.append("      },\n");
            json.append("      \"solarStorms\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"minDuration\": 1.0,\n");
            json.append("        \"maxDuration\": 5.0,\n");
            json.append("        \"minInterval\": 20.0,\n");
            json.append("        \"maxInterval\": 50.0,\n");
            json.append("        \"intensity\": 0.8\n");
            json.append("      }\n");
            json.append("    },\n");

            // Jupiter
            json.append("    {\n");
            json.append("      \"name\": \"Jupiter\",\n");
            json.append("      \"auroras\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"colors\": [\"#8800FF\", \"#FF00FF\", \"#FF0088\"],\n");
            json.append("        \"borealis\": {\"minLatitude\": -75.0, \"maxLatitude\": -55.0},\n");
            json.append("        \"australis\": {\"minLatitude\": 55.0, \"maxLatitude\": 75.0},\n");
            json.append("        \"height\": 200.0,\n");
            json.append("        \"thickness\": 50.0,\n");
            json.append("        \"waveSpeed\": 0.1,\n");
            json.append("        \"waveAmplitude\": 25.0,\n");
            json.append("        \"horizontalFlowSpeed\": 0.05\n");
            json.append("      },\n");
            json.append("      \"solarStorms\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"minDuration\": 5.0,\n");
            json.append("        \"maxDuration\": 15.0,\n");
            json.append("        \"minInterval\": 10.0,\n");
            json.append("        \"maxInterval\": 30.0,\n");
            json.append("        \"intensity\": 1.0\n");
            json.append("      }\n");
            json.append("    },\n");

            // Saturn
            json.append("    {\n");
            json.append("      \"name\": \"Saturn\",\n");
            json.append("      \"auroras\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"colors\": [\"#0088FF\", \"#00FFFF\"],\n");
            json.append("        \"borealis\": {\"minLatitude\": -85.0, \"maxLatitude\": -70.0},\n");
            json.append("        \"australis\": {\"minLatitude\": 70.0, \"maxLatitude\": 85.0},\n");
            json.append("        \"height\": 180.0,\n");
            json.append("        \"thickness\": 40.0,\n");
            json.append("        \"waveSpeed\": 0.08,\n");
            json.append("        \"waveAmplitude\": 20.0,\n");
            json.append("        \"horizontalFlowSpeed\": 0.03\n");
            json.append("      },\n");
            json.append("      \"solarStorms\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"minDuration\": 3.0,\n");
            json.append("        \"maxDuration\": 10.0,\n");
            json.append("        \"minInterval\": 30.0,\n");
            json.append("        \"maxInterval\": 80.0,\n");
            json.append("        \"intensity\": 0.7\n");
            json.append("      }\n");
            json.append("    },\n");

            // Uranus
            json.append("    {\n");
            json.append("      \"name\": \"Uranus\",\n");
            json.append("      \"auroras\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"colors\": [\"#00FFFF\", \"#88FFFF\"],\n");
            json.append("        \"borealis\": {\"minLatitude\": -70.0, \"maxLatitude\": -50.0},\n");
            json.append("        \"australis\": {\"minLatitude\": 50.0, \"maxLatitude\": 70.0},\n");
            json.append("        \"height\": 170.0,\n");
            json.append("        \"thickness\": 35.0,\n");
            json.append("        \"waveSpeed\": 0.06,\n");
            json.append("        \"waveAmplitude\": 18.0,\n");
            json.append("        \"horizontalFlowSpeed\": 0.025\n");
            json.append("      },\n");
            json.append("      \"solarStorms\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"minDuration\": 2.0,\n");
            json.append("        \"maxDuration\": 8.0,\n");
            json.append("        \"minInterval\": 40.0,\n");
            json.append("        \"maxInterval\": 100.0,\n");
            json.append("        \"intensity\": 0.6\n");
            json.append("      }\n");
            json.append("    },\n");

            // Neptune
            json.append("    {\n");
            json.append("      \"name\": \"Neptune\",\n");
            json.append("      \"auroras\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"colors\": [\"#0044FF\", \"#0088FF\"],\n");
            json.append("        \"borealis\": {\"minLatitude\": -80.0, \"maxLatitude\": -65.0},\n");
            json.append("        \"australis\": {\"minLatitude\": 65.0, \"maxLatitude\": 80.0},\n");
            json.append("        \"height\": 160.0,\n");
            json.append("        \"thickness\": 35.0,\n");
            json.append("        \"waveSpeed\": 0.07,\n");
            json.append("        \"waveAmplitude\": 20.0,\n");
            json.append("        \"horizontalFlowSpeed\": 0.03\n");
            json.append("      },\n");
            json.append("      \"solarStorms\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"minDuration\": 2.0,\n");
            json.append("        \"maxDuration\": 7.0,\n");
            json.append("        \"minInterval\": 50.0,\n");
            json.append("        \"maxInterval\": 120.0,\n");
            json.append("        \"intensity\": 0.5\n");
            json.append("      }\n");
            json.append("    }\n");
            json.append("  ],\n");

            // Moons array
            json.append("  \"moons\": [\n");
            json.append("    {\n");
            json.append("      \"name\": \"Titan\",\n");
            json.append("      \"auroras\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"colors\": [\"#FF8800\", \"#FFAA00\"],\n");
            json.append("        \"borealis\": {\"minLatitude\": -85.0, \"maxLatitude\": -70.0},\n");
            json.append("        \"australis\": {\"minLatitude\": 70.0, \"maxLatitude\": 85.0},\n");
            json.append("        \"height\": 100.0,\n");
            json.append("        \"thickness\": 20.0,\n");
            json.append("        \"waveSpeed\": 0.03,\n");
            json.append("        \"waveAmplitude\": 10.0,\n");
            json.append("        \"horizontalFlowSpeed\": 0.015\n");
            json.append("      },\n");
            json.append("      \"solarStorms\": {\n");
            json.append("        \"enabled\": true,\n");
            json.append("        \"minDuration\": 0.5,\n");
            json.append("        \"maxDuration\": 3.0,\n");
            json.append("        \"minInterval\": 15.0,\n");
            json.append("        \"maxInterval\": 40.0,\n");
            json.append("        \"intensity\": 0.4\n");
            json.append("      }\n");
            json.append("    }\n");
            json.append("  ]\n");
            json.append("}\n");

            File configDir = new File("config/astrocraft-151-addon");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
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
        // Simple JSON parser for atmospheric events
        json = json.trim();

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
        // Extract planet name and configs
        String name = extractValue(planetObj, "name");
        if (name == null) return;

        // Parse aurora config
        AuroraConfig auroraConfig = parseAuroraConfig(planetObj);

        // Parse solar storm config
        SolarStormConfig stormConfig = parseSolarStormConfig(planetObj);

        if (auroraConfig != null || stormConfig != null) {
            ATMOSPHERE_MAP.put(name.toLowerCase(), new PlanetAtmosphere(auroraConfig, stormConfig));
        }
    }

    private static AuroraConfig parseAuroraConfig(String planetObj) {
        // Simplified aurora parsing
        int auroraStart = planetObj.indexOf("\"auroras\"");
        if (auroraStart == -1) return null;

        boolean enabled = planetObj.contains("\"enabled\": true");

        // Parse colors
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

        // Parse latitude ranges
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

    public static void updateSolarStorms(String planetName, double currentTime) {
        PlanetAtmosphere atmosphere = getAtmosphere(planetName);
        if (atmosphere == null || atmosphere.solarStorms == null || !atmosphere.solarStorms.enabled) {
            return;
        }

        SolarStormState state = STORM_STATES.computeIfAbsent(planetName.toLowerCase(), k -> new SolarStormState());

        // Initialize if first time
        if (state.nextStormTime == 0) {
            double interval = atmosphere.solarStorms.minInterval +
                    random.nextDouble() * (atmosphere.solarStorms.maxInterval - atmosphere.solarStorms.minInterval);
            state.nextStormTime = currentTime + interval;
        }

        // Check if storm should start
        if (!state.active && currentTime >= state.nextStormTime) {
            state.active = true;
            state.startTime = currentTime;
            double duration = atmosphere.solarStorms.minDuration +
                    random.nextDouble() * (atmosphere.solarStorms.maxDuration - atmosphere.solarStorms.minDuration);
            state.endTime = currentTime + duration;
            state.intensity = atmosphere.solarStorms.intensity;
            System.out.println("Solar storm started on " + planetName + " for " + duration + " days");
        }

        // Check if storm should end
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
}