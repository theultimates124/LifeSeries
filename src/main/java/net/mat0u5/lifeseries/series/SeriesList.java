package net.mat0u5.lifeseries.series;

public enum SeriesList {
    THIRD_LIFE,
    LAST_LIFE,
    DOUBLE_LIFE,
    LIMITED_LIFE,
    SECRET_LIFE,
    WILD_LIFE;

    public static String getStringNameFromSeries(SeriesList series) {
        if (series == THIRD_LIFE) return "thirdlife";
        if (series == LAST_LIFE) return "lastlife";
        if (series == DOUBLE_LIFE) return "doublelife";
        if (series == LIMITED_LIFE) return "limitedlife";
        if (series == SECRET_LIFE) return "secretlife";
        if (series == WILD_LIFE) return "wildlife";
        return "";
    }
}
