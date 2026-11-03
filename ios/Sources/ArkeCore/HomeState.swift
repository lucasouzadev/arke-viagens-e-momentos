public enum TravelContext: String, CaseIterable, Sendable {
    case planning, expenses, savings, reviews, memories
}

public enum PanelDetent: String, CaseIterable, Sendable {
    case explore, balanced, details
}

/// Transient home navigation, independent of persisted travel data.
public struct HomeState: Equatable, Sendable {
    public private(set) var context: TravelContext
    public private(set) var detent: PanelDetent

    public init(context: TravelContext = .planning, detent: PanelDetent = .balanced) {
        self.context = context
        self.detent = detent
    }

    public mutating func selectContext(_ context: TravelContext) { self.context = context }
    public mutating func setDetent(_ detent: PanelDetent) { self.detent = detent }
}
