package engine.upstreams;


import engine.PriceData.LongPosition;
import engine.PriceData.Position;
import engine.PriceData.ShortPosition;
import engine.PriceData.Upstream;
import engine.Serialisation.LocalObject;
import engine.PriceData.Candle;
import engine.components.Ticker;
import engine.PriceData.TimeSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Oanda extends Upstream<Float> {
    private static OkHttpClient client = new OkHttpClient();

    private static LocalObject<String> APIKEY;
    private static LocalObject<String> USERID;

    public static void authenticate(LocalObject<String> userID, LocalObject<String> apiKey) {
        Oanda.USERID = userID;
        Oanda.APIKEY = apiKey;
    }

    private static final int FETCH_SIZE = 5000;

    private static String parseInterval(Duration interval) {
        if (interval.equals(Duration.ofSeconds(5))) {
            return "S5";
        } else if (interval.equals(Duration.ofMinutes(1))) {
            return "M1";
        } else {
            throw new IllegalArgumentException("invalid duration");
        }
    }

    private static JSONObject send(Request.Builder partial) {
//        System.out.println("debug pm: send: start");
//        System.out.println("debug pm: send: " + Oanda.APIKEY);
        Request request = null;
        request = partial
                .addHeader("Authorization", "Bearer " + Oanda.APIKEY.get().get())
                .addHeader("Accept-Datetime-Format", "UNIX")
                .build();
        System.out.println("Oanda::send:" + request);
        JSONObject res = null;
        Response response = null;
        try {
            response = Oanda.client.newCall(request).execute();
            try {
                res = new JSONObject(response.body().string());
            } catch (JSONException e) {
            }
        } catch (IOException e) {
        } finally {
            Optional.ofNullable(response).ifPresent(Response::close);
        }
        return res;
    }


    List<Candle<Float>> fetchCandles (Ticker ticker, ZonedDateTime from, ZonedDateTime to, Duration interval, Integer count) {
//        System.out.println("Oanda::fetchCandles");
        String f = Optional.ofNullable(from)
                .map(dt -> dt.toEpochSecond())
                .map(l -> String.valueOf(l))
                .map(str -> "from=" + str + "&")
                .orElse("");
        String t = Optional.ofNullable(to)
                .filter(dt -> dt.compareTo(ZonedDateTime.now()) <= 0)
                .map(dt -> dt.toEpochSecond())
                .map(l -> String.valueOf(l))
                .map(str -> "to=" + str + "&")
                .orElse("");
        String c = Optional.ofNullable(count)
                .map(i -> String.valueOf(Math.max(i,5))) //account for incomplete last candle
                .map(str -> "count=" + str + "&")
                .orElse("");
        ArrayList<Candle<Float>> candles = new ArrayList<>();

        Request.Builder partial = new Request.Builder()
                .url(String.format(
                        "https://api-fxtrade.oanda.com/v3/accounts/%s/instruments/%s/candles?%s%s%sgranularity=%s",
                        Oanda.USERID.get().orElse("noId"),
                        ticker.getAliasFor(this),
                        f,
                        t,
                        c,
                        Oanda.parseInterval(interval)
                ));
        JSONObject pxObject = Oanda.send(partial);

        try {
            JSONArray delta = pxObject.getJSONArray("candles");
            System.out.println(delta);
            for (int i = 0; i < delta.length(); i++) {
                JSONObject candlestick = delta.getJSONObject(i);

                if (!candlestick.getBoolean("complete")) {
                    continue;
                }
                if (Double.parseDouble(candlestick.getString("time")) < Optional.ofNullable(from).map(ZonedDateTime::toEpochSecond).orElse(0L)) {
                    continue;
                }
                if (Double.parseDouble(candlestick.getString("time")) > Optional.ofNullable(to).map(ZonedDateTime::toEpochSecond).orElse(Long.MAX_VALUE)) {
                    continue;
                }
                candles.add(
                        new Candle<>(
                                ZonedDateTime.ofInstant(Instant.ofEpochSecond(Math.round(Double.parseDouble(candlestick.getString("time")))), ZoneId.systemDefault()),
                                Float.parseFloat(
                                        candlestick.getJSONObject("mid")
                                                .getString("c")
                                )
                        )
                );

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        System.out.println("Oanda::fetchCandles end");

        return candles;

    }

    @Override
    public HashMap<Ticker<Float>, Position<Float>> fetchPositionsNow(Set<Ticker<Float>> tickers) {
        Request.Builder partial = new Request.Builder()
                .url(String.format(
                        "https://api-fxtrade.oanda.com/v3/accounts/%s/openPositions",
                        Oanda.USERID.get().orElse("noId")
                ));

//        System.out.println("debug pm: snapshot: cp0");
//        System.out.println("debug pm: snapshot: " + partial);
        JSONObject allPosObj = Oanda.send(partial);
        JSONArray ap = null;
//        System.out.println("debug pm: snapshot: cp1");
//        System.out.println("debug pm: snapshot: " + allPosObj);
        try {
            ap = allPosObj.getJSONArray("positions");
        } catch (JSONException e) {
//            System.out.println(posRequest);
//            System.out.println(posRequest.headers());
//            System.out.println(allPosObj);
        }
        JSONArray allPos = ap;
        HashMap<Ticker<Float>, Position<Float>> positionMap = new HashMap<>();
        for (int i = 0; i < allPos.length(); i++) {
            try {
                JSONObject posObj = allPos.getJSONObject(i);
                String instrument = posObj.getString("instrument");
                List<Ticker<Float>> tickerMatchLs = tickers.stream()
                        .filter(tk -> tk.getAliasFor(this).equals(instrument))
                        .collect(Collectors.toList());
                if (!tickerMatchLs.isEmpty()) {
                    float netUnits = 0f;
                    float netAmount = 0f;
                    for (JSONObject side : List.of(posObj.getJSONObject("long"), posObj.getJSONObject("short"))) {
                        if (side.getString("units").equals("0")){
                            continue;
                        }
                        netUnits += Float.parseFloat(
                                side.getString("units")
                        );
                        netAmount += Float.parseFloat(
                                side.getString("units")
                        ) * Float.parseFloat(
                                side.getString("averagePrice")
                        );
                    }
                    Float avgPrice = netAmount / netUnits;
                    Ticker<Float> ticker = tickerMatchLs.get(0);
                    Position<Float> position = Position.empty();
                    if (netUnits < 0) {
                        position = new ShortPosition<>(
                                -netUnits,
                                avgPrice
                        );
                    } else if (netUnits > 0) {
                        position = new LongPosition<>(
                                netUnits,
                                avgPrice
                        );
                    }
                    positionMap.put(ticker, position);
                }
            } catch (JSONException e) {
                System.out.println("CRITICAL WARNING");
            };
        }
        return positionMap;
    }

    @Override
    public TimeSeries<Float> fetchCountUntilAtLeast(Ticker<Float> ticker, Duration interval, int ld, ZonedDateTime end) {
//        System.out.println("Oanda::fetchPL");
        ArrayList<Candle<Float>> candles = new ArrayList<>();
        int count = ld;
        while (count > 0) {
            int increment = Math.min(count, Oanda.FETCH_SIZE);
            List<Candle<Float>> delta = fetchCandles(ticker, null, end, interval, increment);
//            System.out.printf("%d %d %d\n",delta.size(), count, increment);
            System.out.println(delta.size());
            end = delta.get(0).getTime().minusSeconds(1);
            candles.addAll(delta);
            count -= delta.size();
        }
//        System.out.println("debug pm: snapshotcf: " +candles.size());
//        System.out.println(candles.get(candles.size()-1).getTime());
//        System.out.println("Oanda::fetchPL end");
        return new TimeSeries<>(candles);
    }

    @Override
    protected TimeSeries<Float> fetchBetweenAtLeast(Ticker<Float> ticker, Duration interval, ZonedDateTime from, ZonedDateTime to) {
//        System.out.println("Oanda::fetchPR");
        if (to.isAfter(ZonedDateTime.now())) {
            to = ZonedDateTime.now();
        }
        ArrayList<Candle<Float>> candles = new ArrayList<>();
        while (!from.isAfter(to)) {
            List<Candle<Float>> delta = fetchCandles(ticker, from, null, interval, Oanda.FETCH_SIZE);
            if (delta.size() == 0) {
                break;
            }
//            System.out.println("Oanda::fetchPR bug start");
            from = delta.get(delta.size()-1).getTime().plus(Duration.ofSeconds(1));
            if (from.isAfter(to)) {
                int sliceLast = Collections.binarySearch(delta.stream().map(Candle::getTime).collect(Collectors.toList()), to);
                if (sliceLast < 0) {
                    sliceLast = -sliceLast - 2;
                }
                delta = delta.subList(0, sliceLast+1);
            }
            candles.addAll(delta);
        }
//        System.out.println("Oanda::fetchPR end");
        return new TimeSeries<>(candles);
    }
}