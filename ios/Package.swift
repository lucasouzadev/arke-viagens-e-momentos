// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "ArkeCore",
    products: [.library(name: "ArkeCore", targets: ["ArkeCore"])],
    targets: [
        .target(name: "ArkeCore"),
        .testTarget(name: "ArkeCoreTests", dependencies: ["ArkeCore"])
    ]
)
