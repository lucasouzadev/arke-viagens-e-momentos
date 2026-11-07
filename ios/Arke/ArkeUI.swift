import SwiftUI
import UIKit
import ImageIO

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


/// Selo (moldura com perfurações) para fotos de capa: o papel envolve a foto, que preenche todo o espaço reservado.
struct ArkeStampShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path(rect)
        let radius: CGFloat = 3
        let spacing: CGFloat = max(10, min(rect.width, rect.height) / 7)
        var x = rect.minX + spacing
        while x < rect.maxX - radius {
            path.addEllipse(in: CGRect(x: x - radius, y: rect.minY - radius, width: radius * 2, height: radius * 2))
            path.addEllipse(in: CGRect(x: x - radius, y: rect.maxY - radius, width: radius * 2, height: radius * 2))
            x += spacing
        }
        var y = rect.minY + spacing
        while y < rect.maxY - radius {
            path.addEllipse(in: CGRect(x: rect.minX - radius, y: y - radius, width: radius * 2, height: radius * 2))
            path.addEllipse(in: CGRect(x: rect.maxX - radius, y: y - radius, width: radius * 2, height: radius * 2))
            y += spacing
        }
        return path
    }
}

struct ArkeStamp: View {
    @Environment(\.colorScheme) private var colorScheme
    let image: UIImage?
    var symbol = "photo"
    var body: some View {
        ZStack {
            Rectangle().fill(colorScheme == .dark ? Color(white: 0.92) : Color.white)
            Group {
                if let image {
                    Image(uiImage: image).resizable().scaledToFill()
                } else {
                    Color(uiColor: .tertiarySystemFill)
                        .overlay { Image(systemName: symbol).font(.system(size: 22)).foregroundStyle(.secondary) }
                }
            }
            .padding(6)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .clipped()
        }
        .clipShape(ArkeStampShape(), style: FillStyle(eoFill: true))
        .overlay(ArkeStampShape().stroke(Color.black.opacity(0.08), lineWidth: 0.5))
        .accessibilityHidden(true)
    }
}

/// Estado vazio de cartão: ícone, título, texto e uma ação (a mesma do "+").
struct ArkeEmptyState: View {
    @Environment(\.colorScheme) private var colorScheme
    let symbol: String
    let title: String
    let message: String
    var actionTitle: String?
    var action: (() -> Void)?
    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: symbol).font(.system(size: 26)).foregroundStyle(ArkeUI.ink)
                .frame(width: 56, height: 56).background(Color(uiColor: .tertiarySystemFill), in: Circle())
                .accessibilityHidden(true)
            Text(title).font(.system(size: 17, weight: .semibold))
            Text(message).font(.system(size: 15)).foregroundStyle(.secondary).multilineTextAlignment(.center)
            if let actionTitle, let action {
                ArkeFooterButton(actionTitle, systemImage: "plus", action: action).padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity).padding(.vertical, 20)
        .accessibilityElement(children: .contain)
    }
}

/// Botão secundário de rodapé do cartão: cápsula de vidro (iOS 26) 44 pt, texto 15 semibold, centralizado.
struct ArkeFooterButton: View {
    let title: String
    var systemImage: String?
    let action: () -> Void
    init(_ title: String, systemImage: String? = nil, action: @escaping () -> Void) {
        self.title = title; self.systemImage = systemImage; self.action = action
    }
    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let systemImage { Image(systemName: systemImage).font(.system(size: 14, weight: .semibold)) }
                Text(title).font(.system(size: 15, weight: .semibold))
            }
            .foregroundStyle(Color(uiColor: .label))
            .padding(.horizontal, 18).frame(minHeight: 44)
            .arkeGlassCapsule().contentShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}


/// Deslizar para a esquerda revela "Excluir" (D-7). Também exposto como ação de acessibilidade.
struct ArkeSwipeToDelete: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme
    let onDelete: () -> Void
    @State private var open = false
    @State private var drag: CGFloat = 0
    private let width: CGFloat = 88

    private var offset: CGFloat { min(0, max(-width, (open ? -width : 0) + drag)) }

    func body(content: Content) -> some View {
        ZStack(alignment: .trailing) {
            Button {
                withAnimation(.snappy(duration: 0.2)) { open = false; drag = 0 }
                onDelete()
            } label: {
                Text("Excluir").font(.system(size: 15, weight: .semibold)).foregroundStyle(.white)
                    .frame(width: width).frame(maxHeight: .infinity)
                    .background(Color(uiColor: .systemRed))
            }
            .buttonStyle(.plain).opacity(offset < -4 ? 1 : 0).accessibilityHidden(true)
            content
                .background(ArkeUI.insetSurface(colorScheme))
                .offset(x: offset)
                .simultaneousGesture(
                    DragGesture(minimumDistance: 20)
                        .onChanged { value in
                            guard abs(value.translation.width) > abs(value.translation.height) else { return }
                            drag = value.translation.width
                        }
                        .onEnded { value in
                            guard abs(value.translation.width) > abs(value.translation.height) else { drag = 0; return }
                            withAnimation(.snappy(duration: 0.2)) {
                                open = (open ? -width : 0) + value.translation.width < -width / 2
                                drag = 0
                            }
                        }
                )
        }
        .clipped()
        .accessibilityAction(named: "Excluir") { onDelete() }
    }
}
extension View {
    func arkeSwipeToDelete(_ onDelete: @escaping () -> Void) -> some View { modifier(ArkeSwipeToDelete(onDelete: onDelete)) }
}


/// Fotos das memórias (até 5): JPEG reduzido guardado em arquivo no aparelho (Application Support, fora do backup),
/// nunca dentro do JSON; miniaturas decodificadas fora da thread principal.
enum ArkeMemoryPhotos {
    static let limit = 5

    private static var directory: URL {
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("ArkeMemories", isDirectory: true)
    }

    static func compress(_ data: Data, maxPixel: Int = 960) -> Data? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil),
              let image = CGImageSourceCreateThumbnailAtIndex(source, 0, [
                kCGImageSourceCreateThumbnailFromImageAlways: true,
                kCGImageSourceCreateThumbnailWithTransform: true,
                kCGImageSourceThumbnailMaxPixelSize: maxPixel,
              ] as CFDictionary),
              let jpeg = UIImage(cgImage: image).jpegData(compressionQuality: 0.74), jpeg.count <= 512_000 else { return nil }
        return jpeg
    }

    static func save(_ photos: [Data]) throws -> [String] {
        guard !photos.isEmpty else { return [] }
        var folder = directory
        try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
        var values = URLResourceValues(); values.isExcludedFromBackup = true
        try? folder.setResourceValues(values)
        var names: [String] = []
        for jpeg in photos.prefix(limit) {
            let name = UUID().uuidString + ".jpg"
            try jpeg.write(to: folder.appendingPathComponent(name), options: [.atomic, .completeFileProtectionUnlessOpen])
            names.append(name)
        }
        return names
    }

    static func thumbnail(named name: String, maxPixel: Int = 360) async -> UIImage? {
        let url = directory.appendingPathComponent(name)
        return await Task.detached(priority: .userInitiated) {
            guard let source = CGImageSourceCreateWithURL(url as CFURL, nil),
                  let image = CGImageSourceCreateThumbnailAtIndex(source, 0, [
                    kCGImageSourceCreateThumbnailFromImageAlways: true,
                    kCGImageSourceCreateThumbnailWithTransform: true,
                    kCGImageSourceThumbnailMaxPixelSize: maxPixel,
                  ] as CFDictionary) else { return nil }
            return UIImage(cgImage: image)
        }.value
    }
}

/// Capa de memória: primeira foto da memória (ou a capa do lugar) em selo, com "+N" quando há mais fotos.
struct ArkeMemoryCover: View {
    let photo: String?
    let fallback: UIImage?
    var extra = 0
    @State private var loaded: UIImage?
    var body: some View {
        ArkeStamp(image: loaded ?? fallback)
            .overlay(alignment: .bottomTrailing) {
                if extra > 0 {
                    Text("+\(extra)").font(.system(size: 13, weight: .bold)).foregroundStyle(.white)
                        .padding(.horizontal, 8).padding(.vertical, 3)
                        .background(.black.opacity(0.6), in: Capsule()).padding(10)
                        .accessibilityLabel("Mais \(extra) fotos")
                }
            }
            .task(id: photo) { if let photo { loaded = await ArkeMemoryPhotos.thumbnail(named: photo) } else { loaded = nil } }
    }
}
