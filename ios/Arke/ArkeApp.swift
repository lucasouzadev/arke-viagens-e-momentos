import SwiftUI

@main
struct ArkeApp: App {
    var body: some Scene {
        WindowGroup {
            HomeView()
                // Candidate in ADR-006, pending final visual approval.
                .tint(Color(red: 76 / 255, green: 114 / 255, blue: 138 / 255))
        }
    }
}
