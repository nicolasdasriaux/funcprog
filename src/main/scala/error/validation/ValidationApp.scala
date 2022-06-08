package error.validation

object ValidationApp {
  def main(args: Array[String]): Unit = {
    object BooleanParser {
      def parse(value: String): Validation[String, Boolean] =
        value.toLowerCase match {
          case "true" | "on" => Validation.succeed(true)
          case "false" | "off" => Validation.succeed(false)
          case _ => Validation.fail(s"Invalid boolean string ($value)")
        }
    }

    case class FeatureFlags(feature1: Boolean, feature2: Boolean)
    case class FeatureFlagsForm(feature1: String, feature2: String)

    object FeatureFlagsForm {
      def parse(form: FeatureFlagsForm): Validation[String, FeatureFlags] = {
        val feature1: Validation[String, Boolean] = BooleanParser.parse(form.feature1)
        val feature2: Validation[String, Boolean] = BooleanParser.parse(form.feature2)
        val features: Validation[String, (Boolean, Boolean)] = feature1 <&> feature2
        val featureFlags: Validation[String, FeatureFlags] = features.map((feature1, feature2) => FeatureFlags(feature1, feature2))
        featureFlags
      }
    }
  }
}
