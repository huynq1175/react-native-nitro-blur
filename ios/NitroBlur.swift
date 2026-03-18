import UIKit

class HybridNitroBlur: HybridNitroBlurSpec {

  // MARK: - View

  private let containerView = UIView()
  private let blurEffectView = BlurEffectView()

  var view: UIView {
    return containerView
  }

  // MARK: - Props

  var tint: BlurTint = .default {
    didSet {
      blurEffectView.tint = tint.toUIBlurEffectStyle()
    }
  }

  var intensity: Double = 50 {
    didSet {
      blurEffectView.intensity = intensity / 100.0
    }
  }

  var blurReductionFactor: Double = 4 {
    didSet {
      // blurReductionFactor is Android-only, no-op on iOS
    }
  }

  var blurTargetId: Double = 0 {
    didSet {
      // blurTargetId is Android-only, no-op on iOS
      // iOS UIVisualEffectView naturally blurs whatever is behind it
    }
  }

  var blurMethod: BlurMethod = .none {
    didSet {
      // blurMethod is Android-only, no-op on iOS
    }
  }

  // MARK: - Init

  override init() {
    super.init()
    containerView.clipsToBounds = true
    blurEffectView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    containerView.addSubview(blurEffectView)

    // Apply defaults
    blurEffectView.tint = tint.toUIBlurEffectStyle()
    blurEffectView.intensity = intensity / 100.0
  }
}

// MARK: - BlurEffectView

/**
 * Based on https://gist.github.com/darrarski/29a2a4515508e385c90b3ffe6f975df7
 */
final class BlurEffectView: UIVisualEffectView {
  var intensity: Double = 0.5 {
    didSet {
      // Clamp between 0.01 and 1.0
      intensity = max(0.01, min(1.0, intensity))
      setNeedsDisplay()
    }
  }

  var tint: UIBlurEffect.Style = .regular {
    didSet {
      visualEffect = UIBlurEffect(style: tint)
    }
  }

  private var visualEffect: UIVisualEffect = UIBlurEffect(style: .regular) {
    didSet {
      setNeedsDisplay()
    }
  }
  private var animator: UIViewPropertyAnimator?

  init() {
    super.init(effect: nil)
  }

  required init?(coder aDecoder: NSCoder) { nil }

  deinit {
    animator?.stopAnimation(true)
  }

  override func draw(_ rect: CGRect) {
    super.draw(rect)

    // BlurView intensity relies on running an animation and making it partially complete.
    // This means there is a continually running animation, which makes Detox hang
    // (it waits for the animation to finish indefinitely).
    // Detect if Detox is running and use on/off behaviour instead.
    if BlurEffectView.isDetoxPresent() {
      effect = intensity > 0 ? visualEffect : nil
      return
    }

    effect = nil
    animator?.stopAnimation(true)
    animator = UIViewPropertyAnimator(duration: 1, curve: .linear) { [unowned self] in
      self.effect = visualEffect
    }
    animator?.fractionComplete = CGFloat(intensity)
  }

  private static func isDetoxPresent() -> Bool {
    let args = ProcessInfo.processInfo.arguments
    return args.contains("-detoxServer") && args.contains("-detoxSessionId")
  }
}

// MARK: - BlurTint → UIBlurEffect.Style

extension BlurTint {
  func toUIBlurEffectStyle() -> UIBlurEffect.Style {
    switch self {
    case .light:
      return .light
    case .dark:
      return .dark
    case .default:
      return .regular
    case .extralight:
      return .extraLight
    case .regular:
      return .regular
    case .prominent:
      return .prominent
    case .systemultrathinmaterial:
      return .systemUltraThinMaterial
    case .systemthinmaterial:
      return .systemThinMaterial
    case .systemmaterial:
      return .systemMaterial
    case .systemthickmaterial:
      return .systemThickMaterial
    case .systemchromematerial:
      return .systemChromeMaterial
    case .systemultrathinmateriallight:
      return .systemUltraThinMaterialLight
    case .systemthinmateriallight:
      return .systemThinMaterialLight
    case .systemmateriallight:
      return .systemMaterialLight
    case .systemthickmateriallight:
      return .systemThickMaterialLight
    case .systemchromemateriallight:
      return .systemChromeMaterialLight
    case .systemultrathinmaterialdark:
      return .systemUltraThinMaterialDark
    case .systemthinmaterialdark:
      return .systemThinMaterialDark
    case .systemmaterialdark:
      return .systemMaterialDark
    case .systemthickmaterialdark:
      return .systemThickMaterialDark
    case .systemchromematerialdark:
      return .systemChromeMaterialDark
    }
  }
}
