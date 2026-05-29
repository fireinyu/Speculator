package engine.modelPredictors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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


    /// NOTES
    /// input shape: model:(1, features) -> ModelPredictor:(features)
    /// output shape: model:(1, targets) -> ModelPredictor:(targets)
    /// features extracted in given order of intervals
    /// prediction returned as single sorted OffsetSeries (collected over all intervals)
    /// prediction is offset from last feature
    private byte[] getModelBytes(String modelName) {
        InputStream inputStream = getClass().getResourceAsStream("/" +modelName+ ".onnx");
        List<Byte> bytes = new ArrayList<>();

        try {
            while (true) {
                int next = inputStream.read();
                if (next < 0) {
                    break;
                }
                bytes.add((byte) next);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        bytes.subList(bytes.size()-20, bytes.size()).stream()
//                .limit(20)
//                .forEach(System.out::println);
//        System.out.println("size: " + bytes.size());
        byte[] res = new byte[bytes.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = bytes.get(i);
        }
        return res;
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
        ((NNPredictor)model).init(getModelBytes(modelName));
    }

    private static class NNExtractor extends FeatureExtractor {
        public NNExtractor(List<Integer> ld) {
            super();
            this.ld = ld;
        }

        private List<Integer> ld;
        @Override
        public List<Float> extract(List<? extends Series> input, float baseline) {
            List<Float> features = new ArrayList<>();
            double logBaseline = Math.log(baseline);
            for (int i = 0; i<input.size(); i++) {
                Series series = input.get(i);
                series.slice(series.size()-ld.get(i), series.size()).get()
                        .stream()
                        .map(px -> (float)(Math.log(px) - logBaseline))
                        .forEach(features::add);
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
            env = OrtEnvironment.getEnvironment();


        }
        private void init(byte[] onnxBytes) {
            try {
                onnxModel = env.createSession(onnxBytes);
            } catch (OrtException e) {
                throw new RuntimeException(e);
            }
        }
        private OrtEnvironment env;
        private OrtSession onnxModel;
        private String requestLabel;
        private String responseLabel;
        private List<Duration> offsets; //model return order

        @Override
        public List<OffsetSeries> predict(List<Float> input, float baseline) {
            Map<String, OnnxTensor> onnxInputs = null;
            float[] inpAx1 = new float[input.size()];
            for (int i = 0; i < inpAx1.length; i++) {
                inpAx1[i] = input.get(i);
            }
            float[][] inp = new float[][]{inpAx1};
            try {
                onnxInputs = Map.of(
                        requestLabel,
                        OnnxTensor.createTensor(env, inp)
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
