import SwiftUI
import UIKit

enum ArkeUI {
    static func sheetSurface(_ scheme: ColorScheme) -> Color {
        Color(uiColor: scheme == .dark ? .secondarySystemGroupedBackground : .systemBackground)
    }
    static func insetSurface(_ scheme: ColorScheme) -> Color {
        Color(uiColor: scheme == .dark ? .systemBackground : .secondarySystemBackground)
    }
    /// Fundo de campo com contraste próprio sobre a superfície do sheet (escuro: preenchimento terciário; claro: inset).
    static func fieldSurface(_ scheme: ColorScheme) -> Color {
        scheme == .dark ? Color(uiColor: .tertiarySystemFill) : Color(uiColor: .secondarySystemBackground)
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
        case .contextTitle: 17
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


/// Vidro circular para o X dos sheets (iOS 26); material translúcido antes disso.
private struct ArkeGlassCircle: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 26, *) {
            content.glassEffect(.regular.interactive(), in: Circle())
        } else {
            content.background(.regularMaterial, in: Circle())
        }
    }
}
extension View {
    func arkeGlassCircle() -> some View { modifier(ArkeGlassCircle()) }
}

/// Campo com rótulo persistente (DESIGN-007): o rótulo continua visível depois de preenchido.
struct ArkeField<Content: View>: View {
    @Environment(\.colorScheme) private var colorScheme
    let title: String
    var optional = false
    let content: Content
    init(_ title: String, optional: Bool = false, @ViewBuilder content: () -> Content) {
        self.title = title; self.optional = optional; self.content = content()
    }
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(title).font(.system(size: 13, weight: .semibold)).foregroundStyle(.secondary)
                Spacer()
                if optional { Text("Opcional").font(.system(size: 13)).foregroundStyle(.secondary) }
            }
            content
                .padding(.horizontal, 16).padding(.vertical, 12)
                .frame(maxWidth: .infinity, minHeight: 52, alignment: .leading)
                .background(ArkeUI.fieldSurface(colorScheme), in: RoundedRectangle(cornerRadius: 20))
        }
    }
}

/// Seletor segmentado com estado selecionado visível: cápsula da marca, texto branco e ✓ (não depende só de cor).
struct ArkeSegmented<Value: Hashable>: View {
    let options: [(value: Value, title: String)]
    @Binding var selection: Value
    var identifier = "segmented"
    @Environment(\.isEnabled) private var isEnabled
    @Namespace private var namespace

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(options.enumerated()), id: \.offset) { _, option in
                let selected = option.value == selection
                Button {
                    withAnimation(.snappy(duration: 0.22)) { selection = option.value }
                } label: {
                    HStack(spacing: 6) {
                        if selected { Image(systemName: "checkmark").font(.system(size: 13, weight: .bold)) }
                        Text(option.title).font(.system(size: 15, weight: selected ? .semibold : .regular))
                    }
                    .foregroundStyle(selected ? Color.white : Color(uiColor: .label))
                    .frame(maxWidth: .infinity, minHeight: 38)
                    .background {
                        if selected {
                            Capsule().fill(ArkeUI.blue)
                                .overlay(Capsule().strokeBorder(ArkeUI.ink, lineWidth: 1))
                                .matchedGeometryEffect(id: "selection", in: namespace)
                        }
                    }
                    .contentShape(Capsule())
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(selected ? .isSelected : [])
                .accessibilityIdentifier("\(identifier).\(option.title)")
            }
        }
        .padding(3)
        .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
        .opacity(isEnabled ? 1 : 0.5)
    }
}

/// Sheet de formulário compacto: altura segue o conteúdo (sem área morta), fundo opaco, X de vidro no topo
/// esquerdo (nome acessível "Cancelar") e primária colada ao conteúdo. Passa para `.large` só se não couber.
struct ArkeFormSheet<Content: View>: View {
    @Environment(\.colorScheme) private var colorScheme
    let title: String
    let primaryTitle: String
    var primaryEnabled = true
    let primaryIdentifier: String
    let onCancel: () -> Void
    let onPrimary: () -> Void
    let content: Content
    @State private var contentHeight: CGFloat = 320

    init(title: String, primaryTitle: String, primaryEnabled: Bool = true, primaryIdentifier: String,
         onCancel: @escaping () -> Void, onPrimary: @escaping () -> Void, @ViewBuilder content: () -> Content) {
        self.title = title; self.primaryTitle = primaryTitle; self.primaryEnabled = primaryEnabled
        self.primaryIdentifier = primaryIdentifier; self.onCancel = onCancel; self.onPrimary = onPrimary
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                Text(title).font(.system(size: 17, weight: .semibold)).lineLimit(1)
                    .accessibilityAddTraits(.isHeader)
                HStack {
                    Button(action: onCancel) {
                        Image(systemName: "xmark").font(.system(size: 16, weight: .semibold))
                            .frame(width: 44, height: 44).arkeGlassCircle().contentShape(Circle())
                    }
                    .buttonStyle(.plain).foregroundStyle(Color(uiColor: .label))
                    .accessibilityLabel("Cancelar").accessibilityIdentifier("sheet.cancel")
                    Spacer()
                }
            }
            .frame(height: 52).padding(.horizontal, 12).padding(.top, 8)
            ScrollView {
                VStack(alignment: .leading, spacing: 14) { content }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20).padding(.top, 8).padding(.bottom, 16)
                    .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height in
                        if abs(contentHeight - height) > 1 { contentHeight = height }
                    }
            }
            .scrollBounceBehavior(.basedOnSize).scrollDismissesKeyboard(.interactively).scrollIndicators(.hidden)
            Button(action: onPrimary) {
                Text(primaryTitle).font(.system(size: 17, weight: .semibold)).foregroundStyle(.white)
                    .frame(maxWidth: .infinity, minHeight: 56).background(ArkeUI.blue, in: Capsule())
                    .contentShape(Capsule())
            }
            .buttonStyle(.plain).opacity(primaryEnabled ? 1 : 0.4).disabled(!primaryEnabled)
            .accessibilityIdentifier(primaryIdentifier)
            .padding(.horizontal, 20).padding(.bottom, 20)
        }
        .background(ArkeUI.sheetSurface(colorScheme).ignoresSafeArea())
        .presentationBackground(ArkeUI.sheetSurface(colorScheme))
        .presentationDetents([.height(60 + contentHeight + 76 + 20), .large])
        .presentationDragIndicator(.hidden)
        .presentationContentInteraction(.resizes)
        .accessibilityAction(.escape) { onCancel() }
    }
}
