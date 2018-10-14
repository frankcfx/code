package com.ibeifeng.spark.mllib

import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, DecisionTreeClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by ibf on 12/07.
  */
object DecisionTreeClassifierDemo {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建 ==> SparkSession构建
    val spark = SparkSession
      .builder()
      .appName("DecisionTreeClassifierDemo")
      .master("local")
      // .enableHiveSupport() // 集成hive，集成hive需要做的操作和1.6完全一样(需要添加pom.xml中的内容以及hive-site.xml文件等相关配置信息)
      .getOrCreate()

    // Load the data stored in LIBSVM format as a DataFrame.
    val data: DataFrame = spark.read.format("libsvm").load("data/mllib/sample_libsvm_data.txt")
    data.show(false)

    // Index labels, adding metadata to the label column.
    // Fit on whole dataset to include all labels in index.
    val labelIndexer: StringIndexerModel = new StringIndexer()
      .setInputCol("label") // 给定当前model的输入列
      .setOutputCol("indexedLabel") // 给定当前模型的输出列
      .fit(data) // 模型的训练
    // Automatically identify categorical features, and index them.
    val featureIndexer: VectorIndexerModel = new VectorIndexer()
      .setInputCol("features") // 给定当前model的输入列
      .setOutputCol("indexedFeatures") // 给定当前模型的输出列
      .setMaxCategories(4) // features with > 4 distinct values are treated as continuous.
      .fit(data)

    // Split the data into training and test sets (30% held out for testing). => 数据作为测试集和训练集
    val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))

    // Train a DecisionTree model.
    val dt: DecisionTreeClassifier = new DecisionTreeClassifier()
      .setLabelCol("indexedLabel") // 给定一个label，其实就是机器学习中的Y， 也就是目标的属性
      .setFeaturesCol("indexedFeatures") // 给定一个变量，其实就是机器学习的X，也就是特征属性
      .setPredictionCol("prediction") // 给定模型的输出列，预测值的列，默认为: prediction
      .setMaxDepth(10)

    // Convert indexed labels back to original labels.
    val labelConverter: IndexToString = new IndexToString()
      .setInputCol("prediction") // 给定一个输入列
      .setOutputCol("predictedLabel") // 给定一个输出列
      .setLabels(labelIndexer.labels)

    // Chain indexers and tree in a Pipeline. => 将构建模型的操作形成这个操作的管道流
    val pipeline: Pipeline = new Pipeline()
      .setStages(Array(labelIndexer, featureIndexer, dt, labelConverter))

    // Train model. This also runs the indexers. => 模型的构建
    val model: PipelineModel = pipeline.fit(trainingData)

    // Make predictions. => 模型预测
    val predictions: DataFrame = model.transform(testData)

    // Select example rows to display.
    predictions.select("predictedLabel", "label", "features").show(5)

    // Select (prediction, true label) and compute test error. => 模型的评估函数
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setPredictionCol("prediction")
      .setMetricName("accuracy") // 计算准确率

    val accuracy = evaluator.evaluate(predictions)
    println("Test Error = " + (1.0 - accuracy)) // 输出错误率

    // 将决策树模型打印
    val treeModel = model.stages(2).asInstanceOf[DecisionTreeClassificationModel]
    println("Learned classification tree model:\n" + treeModel.toDebugString)
  }
}
