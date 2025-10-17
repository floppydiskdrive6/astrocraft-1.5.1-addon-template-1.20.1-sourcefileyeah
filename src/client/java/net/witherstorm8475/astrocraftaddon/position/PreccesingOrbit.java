package net.witherstorm8475.astrocraftaddon.position;

import com.google.gson.*;
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

    // ---------------------------
    // Robust PrecessionConfig
    // ---------------------------
    public static class PrecessionConfig {
        private static Map<String, PrecessionData> PRECESSION_MAP = null;

        public static void load() {
            PRECESSION_MAP = new HashMap<>();
            File file = new File("config/astrocraft-addon.json");
            if (!file.exists()) {
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();

                // ✅ FIX: get the orbital section
                if (root.has("orbital")) {
                    com.google.gson.JsonObject orbital = root.getAsJsonObject("orbital");

                    if (orbital.has("planets")) {
                        parseArray(orbital.getAsJsonArray("planets"));
                    }
                    if (orbital.has("moons")) {
                        parseArray(orbital.getAsJsonArray("moons"));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void parseArray(com.google.gson.JsonArray array) {
            for (com.google.gson.JsonElement element : array) {
                com.google.gson.JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();

                double nodal = obj.has("nodalPrecession") ? obj.get("nodalPrecession").getAsDouble() : 0;
                double apsidal = obj.has("apsidalPrecession") ? obj.get("apsidalPrecession").getAsDouble() : 0;
                double minAxialTilt = obj.has("minAxialTilt") ? obj.get("minAxialTilt").getAsDouble() : 0;
                double maxAxialTilt = obj.has("maxAxialTilt") ? obj.get("maxAxialTilt").getAsDouble() : 0;
                double axialPrecessionPeriod = obj.has("axialPrecessionPeriod") ? obj.get("axialPrecessionPeriod").getAsDouble() : 0;
                double day = obj.has("dayLength") ? obj.get("dayLength").getAsDouble() : 0;

                PRECESSION_MAP.put(name.toLowerCase(),
                        new PrecessionData(nodal, apsidal, minAxialTilt, maxAxialTilt, axialPrecessionPeriod, day)
                );
            }
        }

        public static PrecessionData getPrecession(String planetName) {
            if (PRECESSION_MAP == null) load();
            return PRECESSION_MAP.getOrDefault(planetName.toLowerCase(), new PrecessionData(0, 0, 0, 0, 0, 0));
        }

        public static class PrecessionData {
            public final double nodalPeriod;
            public final double apsidalPeriod;
            public final double minAxialTilt;
            public final double maxAxialTilt;
            public final double axialPrecessionPeriod;
            public final double Day;

            public PrecessionData(double nodal, double apsidal, double minAxialTilt, double maxAxialTilt,
                                  double axialPrecessionPeriod, double Day) {
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
                return;
            }

            // Start from root and recursively process all bodies
            applyPrecessionToBody(root);

        } catch (Exception e) {
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
                    } catch (NoSuchMethodException ignored) {
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
