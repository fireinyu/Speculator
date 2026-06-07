package engine.modelPredictors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.ldap.SortResponseControl;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import engine.PriceData.OffsetCandle;
import engine.PriceData.OffsetSeries;
import engine.PriceData.Series;
import engine.Serialisation.StateMachine;
import engine.Util;
import engine.components.FeatureExtractor;
import engine.components.ModelPredictor;
import engine.components.Predictor;

public abstract class LogNN extends ModelPredictor {

    private static OrtEnvironment env = null;
    private static Map<String, OrtSession> nnModels = new HashMap<>();


    /// NOTES
    /// input shape: model:(1, features) -> ModelPredictor:(features)
    /// output shape: model:(1, targets) -> ModelPredictor:(targets)
    /// features extracted in given order of intervals
    /// prediction returned as single time-sorted OffsetSeries (collected over all intervals)
    /// prediction is offset from last feature
    private OrtSession getModel(String modelName) {
        if (!nnModels.containsKey(modelName)) {
            if (env == null) {
                env = OrtEnvironment.getEnvironment();
            }
            InputStream inputStream = getClass().getResourceAsStream("/" +modelName+ ".onnx") ;
            byte[] bytes;
            try {
                bytes = inputStream.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                LogNN.nnModels.put(modelName, env.createSession(bytes));
            } catch (OrtException e) {
                throw new RuntimeException(e);
            }
        }
        return LogNN.nnModels.get(modelName);

//        bytes.subList(bytes.size()-20, bytes.size()).stream()
//                .limit(20)
//                .forEach(System.out::println);
//        System.out.println("size: " + bytes.size());

    }
    public LogNN(String modelName, String inputLabel, String outputLabel, Util.Pair<LinkedHashMap<Duration, Integer>, LinkedHashMap<Duration, Integer>> dependencies, Map<String, String> settings) {
        super(
                new NNExtractor(new ArrayList<>(dependencies.first.values())),
                new NNPredictor(
                        inputLabel,
                        outputLabel,
                        new ArrayList<>(dependencies.second.keySet()),
                        new ArrayList<>(dependencies.second.values())
                ),
                dependencies,
                settings);
        ((NNPredictor)model).init(getModel(modelName));
    }

    private static class NNExtractor extends FeatureExtractor {
        public NNExtractor(List<Integer> ld) {
            super();
            this.ld = ld;
        }

        private List<Integer> ld;
        @Override
        public float[] extract(List<? extends Series> input, float baseline) {
            float[] features = new float[this.ld.stream().mapToInt(Integer::intValue).sum()];
            int featureIndex = 0;
            double logBaseline = Math.log(baseline);
            for (int i = 0; i<input.size(); i++) {
                Series series = input.get(i);
                float[] prices = series.slice(series.size()-ld.get(i), series.size()).get();
                for (float px : prices) {
                    features[featureIndex++] = (float) (Math.log(px) - logBaseline);
                }
            }
            return features;
        }
    }
    private static class NNPredictor extends Predictor {
        public NNPredictor(
                String requestLabel,
                String responseLabel,
                List<Duration> intervals,
                List<Integer> rd
                ) {
            super();
            this.requestLabel = requestLabel;
            this.responseLabel = responseLabel;
            offsets = new ArrayList<>();
            for (int i = 0; i < intervals.size(); i++) {
                Duration interval = intervals.get(i);
                int dep = rd.get(i)+1;
                for (int j = 1; j < dep; j++) {
                    offsets.add(interval.multipliedBy(j));
                }
            }
            offsets.sort(Duration::compareTo);
        }

        private void init(OrtSession nnModel) {
            onnxModel = nnModel;
        }
        private OrtSession onnxModel;
        private String requestLabel;
        private String responseLabel;
        private List<Duration> offsets; //model return order

        @Override
        public List<OffsetSeries> predict(float[] input, float baseline) {
            Map<String, OnnxTensor> onnxInputs = null;
            float[][] inp = new float[][]{input};
            try {
                onnxInputs = Map.of(
                        requestLabel,
                        OnnxTensor.createTensor(LogNN.env, inp)
                );
            } catch (OrtException e) {
                throw new RuntimeException(e);
            }
            float[] raw = null;
            try (OrtSession.Result results = onnxModel.run(onnxInputs)) {
                OnnxValue result = results.get(responseLabel).get();
                raw =((float[][]) result.getValue())[0];
            } catch (OrtException e) {
                throw new RuntimeException(e);
            }
            List<OffsetCandle> candles = new ArrayList<>();
            for (int i = 0; i < raw.length; i++) {
                candles.add(new OffsetCandle(offsets.get(i), (float) (Math.exp(raw[i]) * baseline)));
            }
            return List.of(new OffsetSeries(candles));
        }
    }
}
