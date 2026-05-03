package engine.PriceData;

import engine.Util;

public class Position  {
    public static Position makeEmpty() {
        return new Position(0);
    }
    public static Position makeLong(double units) {
        return new Position(units);
    }

    public static Position makeShort(double units) {
        return new Position(-units);
    }

    private double units;

    Position(double units) {
        this.units = units;
    }
    public double getUnits() {
        return this.units;
    }

}
