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
    /// Tinta de texto e ações: mesma matiz do azul candidato, com luminosidade ajustada por tema.
    /// Claro: o próprio azul (≈5,1:1 sobre branco). Escuro: versão mais clara (≈6,9:1 sobre o cartão),
    /// já que o azul puro dá ≈3,3:1 no escuro.
    static let ink = Color(uiColor: UIColor { traits in
        traits.userInterfaceStyle == .dark
            ? UIColor(red: 136 / 255, green: 170 / 255, blue: 190 / 255, alpha: 1)
            : UIColor(red: 76 / 255, green: 114 / 255, blue: 138 / 255, alpha: 1)
    })
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
            .presentationDragIndicator(.hidden)
            .presentationContentInteraction(.resizes)
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


/// Vidro nos controles flutuantes (iOS 26); material translúcido antes disso.
private struct ArkeGlassCapsule: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content.glassEffect(.regular.interactive(), in: Capsule())
        } else {
            content.background(.regularMaterial, in: Capsule())
        }
    }
}
extension View {
    func arkeGlassCapsule() -> some View { modifier(ArkeGlassCapsule()) }
}

/// Informa quando o gesto de arrastar para baixo foi bloqueado (`interactiveDismissDisabled`),
/// para abrir a confirmação "Descartar rascunho?". Mantém o delegate original do SwiftUI.
struct ArkeDismissAttempt: UIViewControllerRepresentable {
    let onAttempt: () -> Void

    func makeUIViewController(context: Context) -> Probe { Probe() }
    func updateUIViewController(_ probe: Probe, context: Context) {
        probe.proxy.onAttempt = onAttempt
        // O SwiftUI pode recolocar o próprio delegate ao atualizar (ex.: interactiveDismissDisabled mudou); reinstala.
        DispatchQueue.main.async { probe.install() }
    }

    final class Probe: UIViewController {
        let proxy = Proxy()
        override func didMove(toParent parent: UIViewController?) {
            super.didMove(toParent: parent)
            install()
        }
        override func viewDidAppear(_ animated: Bool) {
            super.viewDidAppear(animated)
            install()
        }
        func install() {
            var node: UIViewController? = self
            while let current = node, current.presentationController == nil { node = current.parent }
            guard let controller = node?.presentationController, controller.delegate !== proxy else { return }
            proxy.original = controller.delegate
            controller.delegate = proxy
        }
    }

    final class Proxy: NSObject, UIAdaptivePresentationControllerDelegate {
        weak var original: UIAdaptivePresentationControllerDelegate?
        var onAttempt: () -> Void = {}

        func presentationControllerDidAttemptToDismiss(_ presentationController: UIPresentationController) {
            onAttempt()
            original?.presentationControllerDidAttemptToDismiss?(presentationController)
        }
        override func responds(to selector: Selector!) -> Bool {
            super.responds(to: selector) || (original as AnyObject?)?.responds(to: selector) == true
        }
        override func forwardingTarget(for selector: Selector!) -> Any? {
            (original as AnyObject?)?.responds(to: selector) == true ? original : super.forwardingTarget(for: selector)
        }
    }
}
