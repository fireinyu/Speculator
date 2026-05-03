package engine.modelPredictors;


import engine.Serialisation.StateLoader;
import engine.Serialisation.StateMachine;
import engine.components.FeatureExtractor;
import engine.components.ModelPredictor;
import engine.PriceData.OffsetCandle;
import engine.PriceData.OffsetSeries;
import engine.components.Predictor;
import engine.PriceData.Series;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
public class NN16842 extends ModelPredictor {

    private static class _Extractor extends FeatureExtractor {

        @Override
        public List<Float> extract(List<? extends Series> input, float baseline) {
            List<Float> S5 = input.get(0).get();
            List<Float> M1 = input.get(1).get();
            List<Float> features = new ArrayList<>();
            for (int i = 16; i > 0; i--) {
                float f = S5.get(S5.size()-i);
                features.add((float) Math.log(f/baseline));
            }
            for (int i = 8; i > 0; i--) {
                float f = M1.get(M1.size()-i);
                features.add((float) Math.log(f/baseline));
            }
            return features;
        }
    }

    private static class _Model extends Predictor {

        private ai.djl.inference.Predictor<List<? extends Float>, List<Float>> predictor;
        private Translator<List<? extends Float>, List<Float>> translator;


        private _Model ( ) {
            this.translator = new Translator<>() {

                @Override
                public List<Float> processOutput(TranslatorContext ctx, NDList list) throws Exception {
                    return Arrays.stream(list.get(0).toArray())
                            .map(number -> number.floatValue())
                            .collect(Collectors.toList());
                }

                @Override
                public NDList processInput(TranslatorContext ctx, List<? extends Float> input) throws Exception {
                    float[] inputArray = new float[input.size()];
                    for (int i = 0; i < input.size(); i++) {
                        inputArray[i] = input.get(i);

                    }
                    return new NDList(NDManager.newBaseManager()
                            .create(inputArray));
                }
            };
        }

        private void _init (InputStream modelStream) {
            ai.djl.Model model = ai.djl.Model.newInstance("model");
            try {
                model.load(modelStream);
            } catch (IOException e) {
                throw new RuntimeException();
            } catch (MalformedModelException e) {
                throw new RuntimeException();
            }
            this.predictor = model.newPredictor(this.translator);
        }

        @Override
        public List<OffsetSeries> predict(List<Float> input, float baseline) {
            List<Float> combinedOutput = null;
            try {
                combinedOutput = this.predictor.predict(input);
            } catch (TranslateException e) {
                throw new RuntimeException();
            }
            ArrayList<OffsetCandle> S5 = new ArrayList<>();
            ArrayList<OffsetCandle> M1 = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                S5.add(
                        new OffsetCandle(Duration.ofSeconds(5*(i+1)), (float) Math.exp(combinedOutput.get(i)) * baseline)
                );
            }
            for (int i = 0; i < 2; i++) {
                M1.add(
                        new OffsetCandle(Duration.ofMinutes(i+1), (float) Math.exp(combinedOutput.get(4 + i)) * baseline )
                );
            }

            return List.of(
                    new OffsetSeries(S5),
                    new OffsetSeries(M1)
            );

        }

    }
    private float bias;

    public NN16842(float bias) {
        super(
                new _Extractor(),
                new _Model(),
                List.of(Duration.ofSeconds(5), Duration.ofMinutes(1)),
                List.of(16, 8),
                List.of(4, 2),
                Map.of("bias", String.valueOf(bias))
        );
        ((_Model)model)._init(getClass().getResourceAsStream("/m_16842.pt"));
        this.bias = bias;
    }

    @Override
    public UserStateLoader<? extends StateMachine<ModelPredictor>> getLoader() {
        return new Loader();
    }

    public static class Loader extends UserStateLoader<ModelPredictor> {
        public Loader() {
            super(List.of("bias"));
        }

        @Override
        public ModelPredictor load(Map<String, String> state) {
            return new NN16842(Float.parseFloat(state.get("bias")));
        }

    }
//
//    @Override
//    public StateLoader<? extends StateMachine<ModelPredictor<Float, Float>>> getLoader() {
//        return new Loader();
//    }

}
