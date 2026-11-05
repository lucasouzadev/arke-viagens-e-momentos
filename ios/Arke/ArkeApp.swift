import SwiftUI

@main
struct ArkeApp: App {
    var body: some Scene {
        WindowGroup {
            HomeView()
                // Candidate in ADR-006, pending final visual approval.
                .tint(ArkeUI.ink)
        }
    }
}
