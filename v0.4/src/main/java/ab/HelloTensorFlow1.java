package ab;

import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.types.TFloat64;

public class HelloTensorFlow1 {

  public static void main(String[] args) throws Exception {

    // https://github.com/tensorflow/java/blob/master/MIGRATING.md
    Graph graph = new Graph();
    Operation a = graph.opBuilder("Const", "a")
        .setAttr("dtype", DataType.DT_DOUBLE)
        .setAttr("value", TFloat64.scalarOf(3.0))
        .build();
    Operation b = graph.opBuilder("Const", "b")
        .setAttr("dtype", DataType.DT_DOUBLE)
        .setAttr("value", TFloat64.scalarOf(2.0))
        .build();
    Operation x = graph.opBuilder("Placeholder", "x")
        .setAttr("dtype", DataType.DT_DOUBLE)
        .build();
    Operation y = graph.opBuilder("Placeholder", "y")
        .setAttr("dtype", DataType.DT_DOUBLE)
        .build();
    Operation ax = graph.opBuilder("Mul", "ax")
        .addInput(a.output(0))
        .addInput(x.output(0))
        .build();
    Operation by = graph.opBuilder("Mul", "by")
        .addInput(b.output(0))
        .addInput(y.output(0))
        .build();
    Operation z = graph.opBuilder("Add", "z")
        .addInput(ax.output(0))
        .addInput(by.output(0))
        .build();
    System.out.println(z.output(0));

    Session sess = new Session(graph);
    TFloat64 tensor = (TFloat64) sess.runner().fetch("z")
        .feed("x", TFloat64.scalarOf(3.0))
        .feed("y", TFloat64.scalarOf(6.0))
        .run().get(0);
    System.out.println(tensor.getDouble()); // 21.0

  }

}
