package com.fahadaltimimi.data;

/**
 * Created by Fahad on 2015-05-17.
 */
public final class UnitConverter {

    private enum UnitsToSIFactors {
        METER("m", "m", 1),
        FEET("ft", "m", 0.3048),
        DEGC("°C", "°C", 1),
        DEGF("°F", "°F", 1),
        PSI("psi", "psi", 1),
        BAR("bar", "psi", 14.5037738),
        KILOGRAM("kg", "kg", 1),
        POUNDS("lbs", "kg", 0.45359237);

        private final String mName;
        private final String mSIName;
        private final double mSIFactor;

        UnitsToSIFactors(String name, String SIName, double SIFactor) {
            mName = name;
            mSIName = SIName;
            mSIFactor = SIFactor;
        }

        public String getName() {
            return mName;
        }

        public String getSIName() {
            return mSIName;
        }

        public double getSIFactor() {
            return mSIFactor;
        }
    }

    private UnitConverter() {
        //
    }

    public static double convertValueToUnits(Double value, String fromUnits, String toUnits) {
        // If converting between degC and degF, use separate formula, otherwise find factor
        if (fromUnits.equals(UnitsToSIFactors.DEGC.getName()) && toUnits.equals(UnitsToSIFactors.DEGF.getName())) {
            return value * (9/5) + 32;
        } else if (fromUnits.equals(UnitsToSIFactors.DEGF.getName()) && toUnits.equals(UnitsToSIFactors.DEGC.getName())) {
            return (value - 32) / (9/5);
        } else {
            // First find factor from from units to SI units
            double factor = 1;
            String siUnits = "";
            for (UnitsToSIFactors unitsToSIFactor : UnitsToSIFactors.values()) {
                if (unitsToSIFactor.getName().equals(fromUnits)) {
                    factor = unitsToSIFactor.getSIFactor();
                    siUnits = unitsToSIFactor.getSIName();
                    break;
                }
            }

            // Now find factor to get from SI units to units
            if (!siUnits.isEmpty()) {
                for (UnitsToSIFactors unitsToSIFactor : UnitsToSIFactors.values()) {
                    if (unitsToSIFactor.getSIName().equals(siUnits) && unitsToSIFactor.getName().equals(toUnits)) {
                        factor = factor * (1 / unitsToSIFactor.getSIFactor());

                        break;
                    }
                }
            }

            return value * factor;
        }
    }
}
