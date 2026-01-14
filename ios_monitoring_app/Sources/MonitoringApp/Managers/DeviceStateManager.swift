import Foundation
import UIKit

class DeviceStateManager {
    
    static let shared = DeviceStateManager()
    
    private var currentLocation: (latitude: Double, longitude: Double)?
    private var networkState: NetworkState = NetworkState()
    private var apnsToken: String?
    
    private init() {}
    
    struct NetworkState {
        var isConnected: Bool = false
        var isWiFi: Bool = false
        var isCellular: Bool = false
    }
    
    struct DeviceState: Codable {
        let deviceId: String
        let deviceName: String
        let osVersion: String
        let modelName: String
        let batteryLevel: Int
        let batteryState: String
        let isEncrypted: Bool
        let isPasscodeSet: Bool
        let isJailbroken: Bool
        let networkConnected: Bool
        let networkType: String
        let latitude: Double?
        let longitude: Double?
        let apnsToken: String?
        let timestamp: Date
        let appVersion: String
    }
    
    func setAPNSToken(_ token: String) {
        apnsToken = token
    }
    
    func updateLocation(latitude: Double, longitude: Double) {
        currentLocation = (latitude, longitude)
    }
    
    func updateNetworkState(isConnected: Bool, isWiFi: Bool, isCellular: Bool) {
        networkState.isConnected = isConnected
        networkState.isWiFi = isWiFi
        networkState.isCellular = isCellular
    }
    
    func collectDeviceState() -> DeviceState {
        let device = UIDevice.current
        device.isBatteryMonitoringEnabled = true
        
        let batteryLevel = Int(device.batteryLevel * 100)
        let batteryState: String = {
            switch device.batteryState {
            case .charging: return "charging"
            case .full: return "full"
            case .unplugged: return "unplugged"
            default: return "unknown"
            }
        }()
        
        let networkType: String = {
            if networkState.isWiFi { return "wifi" }
            if networkState.isCellular { return "cellular" }
            return "none"
        }()
        
        return DeviceState(
            deviceId: getDeviceId(),
            deviceName: device.name,
            osVersion: device.systemVersion,
            modelName: getModelName(),
            batteryLevel: batteryLevel,
            batteryState: batteryState,
            isEncrypted: true,
            isPasscodeSet: isPasscodeSet(),
            isJailbroken: isJailbroken(),
            networkConnected: networkState.isConnected,
            networkType: networkType,
            latitude: currentLocation?.latitude,
            longitude: currentLocation?.longitude,
            apnsToken: apnsToken,
            timestamp: Date(),
            appVersion: getAppVersion()
        )
    }
    
    private func getDeviceId() -> String {
        if let uuid = UIDevice.current.identifierForVendor?.uuidString {
            return uuid
        }
        return UUID().uuidString
    }
    
    private func getModelName() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        return identifier
    }
    
    private func isPasscodeSet() -> Bool {
        return LAContext().canEvaluatePolicy(.deviceOwnerAuthentication, error: nil)
    }
    
    private func isJailbroken() -> Bool {
        #if targetEnvironment(simulator)
        return false
        #else
        let jailbreakPaths = [
            "/Applications/Cydia.app",
            "/Library/MobileSubstrate/MobileSubstrate.dylib",
            "/bin/bash",
            "/usr/sbin/sshd",
            "/etc/apt",
            "/private/var/lib/apt/"
        ]
        
        for path in jailbreakPaths {
            if FileManager.default.fileExists(atPath: path) {
                return true
            }
        }
        
        if let file = fopen("/bin/bash", "r") {
            fclose(file)
            return true
        }
        
        return false
        #endif
    }
    
    private func getAppVersion() -> String {
        return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
    }
}

import LocalAuthentication
