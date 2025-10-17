package net.witherstorm8475.astrocraftaddon.position;

import java.io.*;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class PreccesingOrbit {

    private Object wrappedOrbit;
    protected double periodNodalPrec = 0.0;
    protected double periodApsidalPrec = 0.0;
    protected double storedLongAscending;
    protected double storedLongPeriapsis;
    protected double storedLongMean;
    protected double period;

    public PreccesingOrbit(Object orbitObject, double nodalPrec, double apsidalPrec) throws Exception {
        this.wrappedOrbit = orbitObject;
        this.periodNodalPrec = nodalPrec;
        this.periodApsidalPrec = apsidalPrec;
        Class<?> orbitClass = orbitObject.getClass();
        this.storedLongAscending = getField(orbitClass, orbitObject, "longAscendingAE");
        this.storedLongPeriapsis = getField(orbitClass, orbitObject, "longPeriapsisAE");
        this.storedLongMean = getField(orbitClass, orbitObject, "longMeanAE");
        this.period = getField(orbitClass, orbitObject, "period");
    }

    private double getField(Class<?> clazz, Object obj, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(obj);
    }

    protected double getLongAscending(double time) {
        return periodNodalPrec == 0.0 ? storedLongAscending : storedLongAscending - 360.0 * time / periodNodalPrec;
    }

    protected double getArgPeriapsis(double time) {
        double longAsc = getLongAscending(time);
        if (periodApsidalPrec == 0.0) {
            return storedLongPeriapsis - longAsc;
        } else {
            return storedLongPeriapsis - longAsc + 360.0 * time / periodApsidalPrec;
        }
    }

    protected double getMeanAnomaly(double time) {
        try {
            Method getTimeSinceEpoch = wrappedOrbit.getClass().getDeclaredMethod("getTimeSinceEpoch", double.class);
            getTimeSinceEpoch.setAccessible(true);
            double timeSinceEpoch = (double) getTimeSinceEpoch.invoke(wrappedOrbit, time);
            return storedLongMean - getLongAscending(time) - getArgPeriapsis(time) + 360.0 * timeSinceEpoch / period;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static class PrecessionConfig {
        private static Map<String, PrecessionData> PRECESSION_MAP = null;

        public static void load() {
            PRECESSION_MAP = new HashMap<>();
            try {
                InputStream stream = null;
                stream = PrecessionConfig.class.getResourceAsStream("/astrocraft-151-addon/config/astrocraft-addon.json");
                if (stream == null) {
                    stream = PrecessionConfig.class.getResourceAsStream("astrocraft-151-addon/config/astrocraft-addon.json");
                }
                if (stream == null) {
                    stream = PrecessionConfig.class.getClassLoader().getResourceAsStream("astrocraft-151-addon/config/astrocraft-addon.json");
                }
                if (stream == null) {
                    File configFile = new File("config/astrocraft-addon.json");
                    if (configFile.exists()) {
                        stream = new FileInputStream(configFile);
                        //System.out.println("Loading precession.json from config folder");
                    }
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                reader.close();
                parseJson(jsonContent.toString());
                //System.out.println("Loaded precession data for " + PRECESSION_MAP.size() + " planets");
            } catch (Exception e) {
                System.err.println("Error loading precession.json:");
                e.printStackTrace();
            }
        }

        private static void parseJson(String json) {
            json = json.trim();

            // Find orbital section first
            int orbitalStart = json.indexOf("\"orbital\"");
            int orbitalEnd = findMatchingBrace(json, json.indexOf("{", orbitalStart));
            String orbitalSection = json.substring(orbitalStart, orbitalEnd);

            // Parse planets array within orbital
            int planetsStart = orbitalSection.indexOf("\"planets\"");
            if (planetsStart != -1) {
                planetsStart = orbitalSection.indexOf("[", planetsStart);
                int planetsEnd = findMatchingBracket(orbitalSection, planetsStart);
                if (planetsStart != -1 && planetsEnd != -1) {
                    String planetsArray = orbitalSection.substring(planetsStart + 1, planetsEnd);
                    parseArray(planetsArray);
                }
            }

            // Parse moons array within orbital
            int moonsStart = orbitalSection.indexOf("\"moons\"");
            if (moonsStart != -1) {
                moonsStart = orbitalSection.indexOf("[", moonsStart);
                int moonsEnd = findMatchingBracket(orbitalSection, moonsStart);
                if (moonsStart != -1 && moonsEnd != -1) {
                    String moonsArray = orbitalSection.substring(moonsStart + 1, moonsEnd);
                    parseArray(moonsArray);
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

        private static void parseArray(String arrayContent) {
            String[] objects = arrayContent.split("\\},\\s*\\{");
            for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "").trim();
                String name = null;
                double nodal = 0;
                double apsidal = 0;
                double axialPrecessionPeriod = 0;
                double minAxialTilt = 0;
                double maxAxialTilt = 0;
                double Day = 0;
                String[] pairs = obj.split(",");
                for (String pair : pairs) {
                    String[] kv = pair.split(":");
                    if (kv.length != 2) continue;
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    if (key.equals("name")) {
                        name = value;
                    } else if (key.equals("nodal")) {
                        nodal = Double.parseDouble(value);
                    } else if (key.equals("apsidal")) {
                        apsidal = Double.parseDouble(value);
                    } else if (key.equals("axialPrecessionPeriod")) {
                        axialPrecessionPeriod = Double.parseDouble(value);
                    } else if (key.equals("minAxialTilt")) {
                        minAxialTilt = Double.parseDouble(value);
                    } else if (key.equals("maxAxialTilt")) {
                        maxAxialTilt = Double.parseDouble(value);
                    } else if (key.equals("Day")) {
                        Day = Double.parseDouble(value);
                    }
                }
                if (name != null) {
                    PRECESSION_MAP.put(name.toLowerCase(), new PrecessionData(nodal, apsidal, minAxialTilt, maxAxialTilt, axialPrecessionPeriod, Day));
                }
            }
        }

        public static PrecessionData getPrecession(String planetName) {
            if (PRECESSION_MAP == null) {
                load();
            }
            return PRECESSION_MAP.getOrDefault(planetName.toLowerCase(), new PrecessionData(0, 0, 0, 0, 0, 0));
        }

        public static class PrecessionData {
            public final double nodalPeriod;
            public final double apsidalPeriod;
            public final double minAxialTilt;
            public final double maxAxialTilt;
            public final double axialPrecessionPeriod;
            public final double Day;

            public PrecessionData(double nodal, double apsidal, double minAxialTilt, double maxAxialTilt, double axialPrecessionPeriod, double Day) {
                this.nodalPeriod = nodal;
                this.apsidalPeriod = apsidal;
                this.minAxialTilt = minAxialTilt;
                this.maxAxialTilt = maxAxialTilt;
                this.axialPrecessionPeriod = axialPrecessionPeriod;
                this.Day = Day;
            }
        }
    }

    public static void applyPrecessionToPlanets() {
        try {
            PrecessionConfig.load();

            Class<?> planetManagerClass = Class.forName("mod.lwhrvw.astrocraft.planets.PlanetManager");
            Field rootField = planetManagerClass.getDeclaredField("root");
            rootField.setAccessible(true);
            Object root = rootField.get(null);

            if (root == null) {
                System.err.println("ERROR: PlanetManager.root is null - planets not loaded yet");
                return;
            }

            //System.out.println("[AstroCraft Addon] Applying precession to all celestial bodies...");

            // Start from root and recursively process all bodies
            applyPrecessionToBody(root);

            //System.out.println("[AstroCraft Addon] Precession application complete!");

        } catch (Exception e) {
            System.err.println("Error applying precession:");
            e.printStackTrace();
        }
    }

    private static void applyPrecessionToBody(Object body) throws Exception {
        Method getID = body.getClass().getMethod("getID");
        String id = (String) getID.invoke(body);

        // Get positioner field
        Field positionerField = null;
        Class<?> searchClass = body.getClass();
        while (searchClass != null && positionerField == null) {
            try {
                positionerField = searchClass.getDeclaredField("positioner");
                positionerField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                searchClass = searchClass.getSuperclass();
            }
        }

        if (positionerField != null) {
            Object positioner = positionerField.get(body);

            if (positioner != null && positioner.getClass().getSimpleName().equals("Orbit")) {
                String[] parts = id.split("\\.");
                String bodyName = parts[parts.length - 1];

                PrecessionConfig.PrecessionData precData = PrecessionConfig.getPrecession(bodyName);

                if (precData.nodalPeriod != 0 || precData.apsidalPeriod != 0) {
                    try {
                        Method setPrecession = positioner.getClass().getMethod("astrocraftAddon$setPrecession", double.class, double.class);
                        setPrecession.invoke(positioner, precData.nodalPeriod, precData.apsidalPeriod);
                        //System.out.println("[AstroCraft Addon] Applied precession to " + bodyName + " (" + id + "): nodal=" + precData.nodalPeriod + ", apsidal=" + precData.apsidalPeriod);
                    } catch (NoSuchMethodException e) {
                        System.out.println("[AstroCraft Addon] Warning: Could not find setPrecession method for " + bodyName);
                    }
                }
            }
        }

        // Get children and recursively process them
        HashMap<?, ?> children = null;
        searchClass = body.getClass();

        while (searchClass != null && children == null) {
            Field[] fields = searchClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(HashMap.class)) {
                    field.setAccessible(true);
                    children = (HashMap<?, ?>) field.get(body);
                    if (children != null) {
                        break;
                    }
                }
            }
            searchClass = searchClass.getSuperclass();
        }

        // Recursively apply precession to all children
        if (children != null && !children.isEmpty()) {
            for (Object child : children.values()) {
                if (child != null) {
                    applyPrecessionToBody(child);
                }
            }
        }
    }
}
