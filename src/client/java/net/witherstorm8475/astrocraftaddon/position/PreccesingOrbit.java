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
                stream = PrecessionConfig.class.getResourceAsStream("/astrocraft-151-addon/precession.json");
                if (stream == null) {
                    stream = PrecessionConfig.class.getResourceAsStream("astrocraft-151-addon/precession.json");
                }
                if (stream == null) {
                    stream = PrecessionConfig.class.getClassLoader().getResourceAsStream("astrocraft-151-addon/precession.json");
                }
                if (stream == null) {
                    File configFile = new File("config/astrocraft-151-addon/precession.json");
                    if (configFile.exists()) {
                        stream = new FileInputStream(configFile);
                        System.out.println("Loading precession.json from config folder");
                    }
                }
                if (stream == null) {
                    System.out.println("precession.json not found in any location, generating default...");
                    generateDefaultPrecessionJson();
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
                //System.out.println("Loaded precession data for " + PRECESSION_MAP.size() + " planets");
            } catch (Exception e) {
                System.err.println("Error loading precession.json:");
                e.printStackTrace();
            }
        }

        private static void generateDefaultPrecessionJson() {
            try {
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"planets\": [\n");
                json.append("    {\"name\": \"Mercury\", \"nodal\": 325513, \"apsidal\": 280000, \"minAxialTilt\": 0.01, \"maxAxialTilt\": 0.034, \"axialPrecessionPeriod\": 325000, \"siderealDay\": 176},\n");
                json.append("    {\"name\": \"Venus\", \"nodal\": 29000, \"apsidal\": 29000, \"minAxialTilt\": 177.3, \"maxAxialTilt\": 177.4, \"axialPrecessionPeriod\": 29000, \"siderealDay\": -116},\n");
                json.append("    {\"name\": \"Earth\", \"nodal\": 25772, \"apsidal\": 112000, \"minAxialTilt\": 22.1, \"maxAxialTilt\": 24.5, \"axialPrecessionPeriod\": 25772, \"siderealDay\": 1},\n");
                json.append("    {\"name\": \"Mars\", \"nodal\": 170000, \"apsidal\": 7300, \"minAxialTilt\": 22.04, \"maxAxialTilt\": 26.14, \"axialPrecessionPeriod\": 170000, \"siderealDay\": 1.027},\n");
                json.append("    {\"name\": \"Jupiter\", \"nodal\": 50687, \"apsidal\": 200000, \"minAxialTilt\": 3.13, \"maxAxialTilt\": 3.13, \"axialPrecessionPeriod\": 0, \"siderealDay\": 0.41354},\n");
                json.append("    {\"name\": \"Saturn\", \"nodal\": 50687, \"apsidal\": 1400000, \"minAxialTilt\": 26.73, \"maxAxialTilt\": 26.73, \"axialPrecessionPeriod\": 0, \"siderealDay\": 0.44401},\n");
                json.append("    {\"name\": \"Uranus\", \"nodal\": 0, \"apsidal\": 0, \"minAxialTilt\": 97.77, \"maxAxialTilt\": 97.77, \"axialPrecessionPeriod\": 0, \"siderealDay\": -0.71833},\n");
                json.append("    {\"name\": \"Neptune\", \"nodal\": 0, \"apsidal\": 0, \"minAxialTilt\": 28.32, \"maxAxialTilt\": 28.32, \"axialPrecessionPeriod\": 0, \"siderealDay\": 0.67125},\n");
                json.append("    {\"name\": \"Pluto\", \"nodal\": 20000, \"apsidal\": 19951, \"minAxialTilt\": 102, \"maxAxialTilt\": 126, \"axialPrecessionPeriod\": 3000000, \"siderealDay\": 6.387}\n");
                json.append("  ],\n");  // close planets array
                json.append("  \"moons\": [\n");
                json.append("    {\"name\": \"Moon\", \"minAxialTilt\": 1.54, \"maxAxialTilt\": 1.54, \"axialPrecessionPeriod\": 0, \"siderealDay\": 1}\n");
                json.append("  ]\n");  // close moons array
                json.append("}\n");   // close JSON object

                File configDir = new File("config/astrocraft-151-addon");
                if (!configDir.exists()) {
                    configDir.mkdirs();
                }
                File jsonFile = new File(configDir, "precession.json");
                FileWriter writer = new FileWriter(jsonFile);
                writer.write(json.toString());
                writer.close();
                System.out.println("Generated precession.json at: " + jsonFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error generating precession.json:");
                e.printStackTrace();
            }
        }

        private static void parseJson(String json) {
            json = json.trim();

            // Parse planets array
            int planetsStart = json.indexOf("\"planets\"");
            if (planetsStart != -1) {
                planetsStart = json.indexOf("[", planetsStart);
                int planetsEnd = json.indexOf("]", planetsStart);
                if (planetsStart != -1 && planetsEnd != -1) {
                    String planetsArray = json.substring(planetsStart + 1, planetsEnd);
                    parseArray(planetsArray);
                }
            }

            // Parse moons array
            int moonsStart = json.indexOf("\"moons\"");
            if (moonsStart != -1) {
                moonsStart = json.indexOf("[", moonsStart);
                int moonsEnd = json.indexOf("]", moonsStart);
                if (moonsStart != -1 && moonsEnd != -1) {
                    String moonsArray = json.substring(moonsStart + 1, moonsEnd);
                    parseArray(moonsArray);
                }
            }
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
                double siderealDay = 0;
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
                    } else if (key.equals("siderealDay")) {
                        siderealDay = Double.parseDouble(value);
                    }
                }
                if (name != null) {
                    PRECESSION_MAP.put(name.toLowerCase(), new PrecessionData(nodal, apsidal, minAxialTilt, maxAxialTilt, axialPrecessionPeriod, siderealDay));
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
            public final double siderealDay;

            public PrecessionData(double nodal, double apsidal, double minAxialTilt, double maxAxialTilt, double axialPrecessionPeriod, double siderealDay) {
                this.nodalPeriod = nodal;
                this.apsidalPeriod = apsidal;
                this.minAxialTilt = minAxialTilt;
                this.maxAxialTilt = maxAxialTilt;
                this.axialPrecessionPeriod = axialPrecessionPeriod;
                this.siderealDay = siderealDay;
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

            //System.out.println("DEBUG: Got root: " + root);

            if (root == null) {
                System.err.println("ERROR: PlanetManager.root is null - planets not loaded yet");
                return;
            }

            // Find and process ONLY the sun body
            Method findChild = root.getClass().getMethod("findChild", String.class);
            Object sun = findChild.invoke(root, "sun");

            if (sun != null) {
                //System.out.println("DEBUG: Found sun, processing its children...");
                applyPrecessionToBody(sun);
            } else {
                System.err.println("ERROR: Could not find sun body");
            }

            //System.out.println("Precession application complete!");

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

        if (positionerField == null) {
            return; // Skip if no positioner
        }

        Object positioner = positionerField.get(body);

        if (positioner != null && positioner.getClass().getSimpleName().equals("Orbit")) {
            String[] parts = id.split("\\.");
            String planetName = parts[parts.length - 1];

            PrecessionConfig.PrecessionData precData = PrecessionConfig.getPrecession(planetName);

            if (precData.nodalPeriod != 0 || precData.apsidalPeriod != 0) {
                Method setPrecession = positioner.getClass().getMethod("astrocraftAddon$setPrecession", double.class, double.class);
                setPrecession.invoke(positioner, precData.nodalPeriod, precData.apsidalPeriod);

                //System.out.println("Applied precession to " + planetName + " (" + id + "): nodal=" + precData.nodalPeriod + ", apsidal=" + precData.apsidalPeriod);
            }
        }

        // Get children HashMap
        HashMap children = null;
        searchClass = body.getClass();
        while (searchClass != null && children == null) {
            Field[] fields = searchClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType().equals(HashMap.class)) {
                    children = (HashMap) field.get(body);
                    break;
                }
            }
            searchClass = searchClass.getSuperclass();
        }

        if (children != null) {
            for (Object child : children.values()) {
                applyPrecessionToBody(child);
            }
        }
    }
}
