package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.series.Blacklist;
import net.mat0u5.lifeseries.series.Series;
import net.mat0u5.lifeseries.series.SeriesList;

public class SecretLife extends Series {
    @Override
    public SeriesList getSeries() {
        return SeriesList.SECRET_LIFE;
    }
    @Override
    public Blacklist createBlacklist() {
        return null;
    }
    @Override
    public String getResourcepackURL() {
        return "https://github.com/Mat0u5/LifeSeries-Resources/releases/download/release-lastlife-f2bffdb14903964db62d503e844f554d72d5388c/RP.zip";
    }
    @Override
    public String getResourcepackSHA1() {
        return "ff00976ff0d848f78fcab1f3d35afddc1e3c2589";
    }
}
