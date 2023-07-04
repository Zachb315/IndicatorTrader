package com.trader.trader.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MACD {
    public static List<Double> macdLine(List<Double> prices, int shortPeriod, int longPeriod) {
        List<Double> macd = new ArrayList<>();
        List<Double> ema12 = getEMA(prices, shortPeriod);
        List<Double> ema26 = getEMA(prices, longPeriod);
        for (int i=0; i<ema12.size(); i++) {
            double macdVal = ema12.get(i)-ema26.get(i);
            macd.add(macdVal);
        }
        return macd;
    }

    public static List<Double> getEMA(List<Double> prices, int period) {
        List<Double> ema = new ArrayList<>();
        double sum=0.0;
        for (int i=0; i<period; i++) {
            sum+=prices.get(i);
        }
        double sma = sum/period;
        double sm = 2.0/(period+1);
        ema.add(sma);

        for (int i=1; i<prices.size(); i++) {
            double curr = prices.get(i);
            double emaVal = (curr-ema.get(i-1)) * sm+ema.get(i-1);
            ema.add(emaVal);
        }
        return ema;
    }

    public static List<Double> signalLine(List<Double> macd, int period) {
        List<Double> signalLine = new ArrayList<>();

        for (int i=period; i<macd.size(); i++) {
            double sum=0.0;
            for (int j=i-period; j<=i; j++) sum+=macd.get(j);
            signalLine.add(sum/period);
        }
        return signalLine;

    }
}
