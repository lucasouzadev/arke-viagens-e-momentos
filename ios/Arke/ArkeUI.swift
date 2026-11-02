import SwiftUI

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
    @Environment(\.colorScheme) private var colorScheme
    let dismissIdentifier: String
    let onDismiss: () -> Void
    let enabled: Bool
    let content: Content

    init(dismissIdentifier: String, enabled: Bool = true, onDismiss: @escaping () -> Void, @ViewBuilder content: () -> Content) {
        self.dismissIdentifier = dismissIdentifier
        self.onDismiss = onDismiss
        self.enabled = enabled
        self.content = content()
    }

    var body: some View {
        VStack(spacing: 0) {
            Button(action: onDismiss) {
                Capsule().fill(Color(uiColor: .secondaryLabel)).frame(width: 36, height: 4)
                    .frame(width: 64, height: 16)
                    .background(ArkeUI.sheetSurface(colorScheme), in: Capsule())
                    .frame(maxWidth: .infinity, minHeight: 48).contentShape(Rectangle())
            }
            .buttonStyle(.plain).disabled(!enabled)
            .accessibilityLabel("Recolher painel").accessibilityHint("Volta à tela anterior.")
            .accessibilityIdentifier(dismissIdentifier)
            content
                .background(ArkeUI.sheetSurface(colorScheme))
                .clipShape(RoundedRectangle(cornerRadius: 28))
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier(dismissIdentifier + ".card")
        }
        .padding(.horizontal, 12).padding(.bottom, 12)
        .presentationBackground(.clear).presentationDragIndicator(.hidden)
        .presentationCornerRadius(0)
    }
}

private struct ArkeRecedingSurface: ViewModifier {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    let presented: Bool
    func body(content: Content) -> some View {
        content
            .clipShape(RoundedRectangle(cornerRadius: presented ? 28 : 0))
            .scaleEffect(presented ? 0.96 : 1, anchor: .top)
            .animation(reduceMotion ? nil : .easeOut(duration: 0.22), value: presented)
            .background(Color(uiColor: .systemGroupedBackground))
    }
}

extension View {
    func arkeReceding(presented: Bool) -> some View { modifier(ArkeRecedingSurface(presented: presented)) }
}
