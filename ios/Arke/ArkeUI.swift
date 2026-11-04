import SwiftUI
import UIKit

enum ArkeUI {
    static func sheetSurface(_ scheme: ColorScheme) -> Color {
        Color(uiColor: scheme == .dark ? .secondarySystemGroupedBackground : .systemBackground)
    }
    static func insetSurface(_ scheme: ColorScheme) -> Color {
        Color(uiColor: scheme == .dark ? .systemBackground : .secondarySystemBackground)
    }
    static let margin: CGFloat = 20
    static let controlHeight: CGFloat = 48
    static let fieldRadius: CGFloat = 12
    static let cardRadius: CGFloat = 20
    static let blue = Color(red: 76 / 255, green: 114 / 255, blue: 138 / 255)
}

enum ArkeTextRole {
    case appTitle, screenTitle, cardTitle, contextTitle, body, label, caption, button
    var size: CGFloat {
        switch self {
        case .appTitle, .cardTitle: 22
        case .screenTitle: 28
        case .contextTitle: 18
        case .body, .button: 16
        case .label, .caption: 14
        }
    }
    var weight: Font.Weight {
        switch self {
        case .screenTitle: .bold
        case .body, .caption: .regular
        default: .semibold
        }
    }
}

private struct ArkeTextStyle: ViewModifier {
    let role: ArkeTextRole
    @ScaledMetric private var size: CGFloat
    init(role: ArkeTextRole) {
        self.role = role
        _size = ScaledMetric(wrappedValue: role.size, relativeTo: .body)
    }
    func body(content: Content) -> some View {
        content.font(.system(size: size, weight: role.weight)).lineSpacing(4)
    }
}

extension View {
    func arkeText(_ role: ArkeTextRole) -> some View { modifier(ArkeTextStyle(role: role)) }
}

/// Native sheet presentation with its visible card inset from the system edges.
struct ArkeFloatingSheet<Content: View>: View {
    let dismissIdentifier: String
    let content: Content

    init(dismissIdentifier: String, @ViewBuilder content: () -> Content) {
        self.dismissIdentifier = dismissIdentifier
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            content
        }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(dismissIdentifier + ".card")
            .presentationDragIndicator(.visible)
            .presentationContentInteraction(.scrolls)
    }
}


private struct ArkeRecedingSurface: ViewModifier {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let presented: Bool
    func body(content: Content) -> some View {
        content // The native presentation owns recession and safe areas.
    }
}

extension View {
    func arkeReceding(presented: Bool) -> some View { modifier(ArkeRecedingSurface(presented: presented)) }
}

struct ArkeScrollEdges: ViewModifier {
    var active = true

    func body(content: Content) -> some View {
        if #available(iOS 26, *), active {
            content.scrollIndicators(.hidden).scrollEdgeEffectStyle(.soft, for: [.top, .bottom])
        } else {
            content.scrollIndicators(.hidden)
        }
    }
}
extension View {
    func arkeScrollEdges() -> some View { modifier(ArkeScrollEdges()) }
}

/// UIKit owns keyboard occlusion; SwiftUI receives the actual visible viewport.
struct ArkeKeyboardViewport<Content: View>: UIViewControllerRepresentable {
    let content: Content

    init(@ViewBuilder content: () -> Content) { self.content = content() }

    func makeUIViewController(context: Context) -> ArkeKeyboardViewportController<Content> {
        ArkeKeyboardViewportController(content: content)
    }

    func updateUIViewController(_ controller: ArkeKeyboardViewportController<Content>, context: Context) {
        controller.host.rootView = content
    }
}

@MainActor
final class ArkeKeyboardViewportController<Content: View>: UIViewController {
    let host: UIHostingController<Content>

    init(content: Content) {
        host = UIHostingController(rootView: content)
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("Use init(content:)") }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        host.safeAreaRegions = .container
        addChild(host)
        host.view.backgroundColor = .clear
        host.view.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(host.view)
        NSLayoutConstraint.activate([
            host.view.topAnchor.constraint(equalTo: view.topAnchor),
            host.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            host.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            host.view.bottomAnchor.constraint(equalTo: view.keyboardLayoutGuide.topAnchor),
        ])
        host.didMove(toParent: self)
    }
}
