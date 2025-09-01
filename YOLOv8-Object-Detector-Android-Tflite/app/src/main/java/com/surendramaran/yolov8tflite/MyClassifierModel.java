package com.surendramaran.yolov8tflite;

import android.content.Context;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.Tensor.QuantizationParams;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.metadata.schema.NormalizationOptions;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class MyClassifierModel {
  private final Metadata metadata;
  private final Model model;
  private static final String MODEL_NAME = "best_full_integer_quant.tflite";
  private ImageProcessor imagePreprocessor;
  private TensorProcessor outputPostprocessor;

  public static class Outputs {
    private final TensorBuffer output;
    private final List<String> outputLabels;
    private final TensorProcessor outputPostprocessor;

    public List<Category> getOutputAsCategoryList() {
      return new TensorLabel(outputLabels, postprocessOutput(output)).getCategoryList();
    }

    Outputs(Metadata metadata, TensorProcessor outputPostprocessor) {
      output = TensorBuffer.createFixedSize(metadata.getOutputShape(), metadata.getOutputType());
      outputLabels = metadata.getOutputLabels();
      this.outputPostprocessor = outputPostprocessor;
    }

    Map<Integer, Object> getBuffer() {
      Map<Integer, Object> outputs = new HashMap<>();
      outputs.put(0, output.getBuffer());
      return outputs;
    }

    private TensorBuffer postprocessOutput(TensorBuffer tensorBuffer) {
      return outputPostprocessor.process(tensorBuffer);
    }
  }

  public static class Metadata {
    private final int[] imageShape;
    private final DataType imageDataType;
    private final QuantizationParams imageQuantizationParams;
    private final int[] outputShape;
    private final DataType outputDataType;
    private final QuantizationParams outputQuantizationParams;
    private final List<String> outputLabels;

    public Metadata(ByteBuffer buffer, Model model) throws IOException {
      MetadataExtractor extractor = new MetadataExtractor(buffer);
      Tensor imageTensor = model.getInputTensor(0);
      imageShape = imageTensor.shape();
      imageDataType = imageTensor.dataType();
      imageQuantizationParams = imageTensor.quantizationParams();
      Tensor outputTensor = model.getOutputTensor(0);
      outputShape = outputTensor.shape();
      outputDataType = outputTensor.dataType();
      outputQuantizationParams = outputTensor.quantizationParams();
      String outputLabelsFileName =
          extractor.getOutputTensorMetadata(0).associatedFiles(0).name();
      outputLabels = FileUtil.loadLabels(extractor.getAssociatedFile(outputLabelsFileName));
    }

    public int[] getImageShape() {
      return Arrays.copyOf(imageShape, imageShape.length);
    }

    public DataType getImageType() {
      return imageDataType;
    }

    public QuantizationParams getImageQuantizationParams() {
      return imageQuantizationParams;
    }

    public int[] getOutputShape() {
      return Arrays.copyOf(outputShape, outputShape.length);
    }

    public DataType getOutputType() {
      return outputDataType;
    }

    public QuantizationParams getOutputQuantizationParams() {
      return outputQuantizationParams;
    }

    public List<String> getOutputLabels() {
      return outputLabels;
    }
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public static MyClassifierModel newInstance(Context context) throws IOException {
    return newInstance(context, MODEL_NAME, new Model.Options.Builder().build());
  }

  public static MyClassifierModel newInstance(Context context, String modelPath) throws IOException {
    return newInstance(context, modelPath, new Model.Options.Builder().build());
  }

  public static MyClassifierModel newInstance(Context context, Model.Options runningOptions) throws IOException {
    return newInstance(context, MODEL_NAME, runningOptions);
  }

  public static MyClassifierModel newInstance(Context context, String modelPath, Model.Options runningOptions) throws IOException {
    Model model = Model.createModel(context, modelPath, runningOptions);
    Metadata metadata = new Metadata(model.getData(), model);
    MyClassifierModel instance = new MyClassifierModel(model, metadata);
    instance.resetImagePreprocessor(
        instance.buildDefaultImagePreprocessor());
    instance.resetOutputPostprocessor(
        instance.buildDefaultOutputPostprocessor());
    return instance;
  }


  public void resetImagePreprocessor(ImageProcessor processor) {
    imagePreprocessor = processor;
  }

  public void resetOutputPostprocessor(TensorProcessor processor) {
    outputPostprocessor = processor;
  }


  public Outputs process(TensorImage image) {
    Outputs outputs = new Outputs(metadata, outputPostprocessor);
    Object[] inputBuffers = preprocessInputs(image);
    model.run(inputBuffers, outputs.getBuffer());
    return outputs;
  }


  public void close() {
    model.close();
  }

  private MyClassifierModel(Model model, Metadata metadata) {
    this.model = model;
    this.metadata = metadata;
  }

  private ImageProcessor buildDefaultImagePreprocessor() {
    ImageProcessor.Builder builder = new ImageProcessor.Builder()
        .add(new ResizeOp(
            metadata.getImageShape()[1],
            metadata.getImageShape()[2],
            ResizeMethod.NEAREST_NEIGHBOR))
        .add(new QuantizeOp(
            metadata.getImageQuantizationParams().getZeroPoint(),
            metadata.getImageQuantizationParams().getScale()))
        .add(new CastOp(metadata.getImageType()));
    return builder.build();
  }

  private TensorProcessor buildDefaultOutputPostprocessor() {
    TensorProcessor.Builder builder = new TensorProcessor.Builder()
        .add(new DequantizeOp(
            metadata.getOutputQuantizationParams().getZeroPoint(),
            metadata.getOutputQuantizationParams().getScale()));
    return builder.build();
  }

  private Object[] preprocessInputs(TensorImage image) {
    image = imagePreprocessor.process(image);
    return new Object[] {image.getBuffer()};
  }
}

