import Foundation
import UIKit
import CoreLocation
import Network

class DeviceMonitoringService: NSObject {
    
    static let shared = DeviceMonitoringService()
    
    private let locationManager = CLLocationManager()
    private let networkMonitor = NWPathMonitor()
    private var monitoringTimer: Timer?
    private let monitorInterval: TimeInterval = 60
    
    private override init() {
        super.init()
        setupLocationManager()
        setupNetworkMonitor()
    }
    
    private func setupLocationManager() {
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
    }
    
    private func setupNetworkMonitor() {
        networkMonitor.pathUpdateHandler = { path in
            DeviceStateManager.shared.updateNetworkState(
                isConnected: path.status == .satisfied,
                isWiFi: path.usesInterfaceType(.wifi),
                isCellular: path.usesInterfaceType(.cellular)
            )
        }
        networkMonitor.start(queue: DispatchQueue.global(qos: .background))
    }
    
    func startMonitoring() {
        locationManager.requestAlwaysAuthorization()
        locationManager.startUpdatingLocation()
        
        monitoringTimer = Timer.scheduledTimer(withTimeInterval: monitorInterval, repeats: true) { [weak self] _ in
            self?.collectDeviceData()
        }
        
        collectDeviceData()
        
        print("[MonitoringService] Started monitoring")
    }
    
    func stopMonitoring() {
        monitoringTimer?.invalidate()
        monitoringTimer = nil
        locationManager.stopUpdatingLocation()
        networkMonitor.cancel()
        
        print("[MonitoringService] Stopped monitoring")
    }
    
    func collectDeviceData() {
        let deviceState = DeviceStateManager.shared.collectDeviceState()
        
        RemotePolicyManager.shared.reportDeviceState(deviceState) { success in
            if success {
                print("[MonitoringService] Device state reported successfully")
            } else {
                print("[MonitoringService] Failed to report device state")
            }
        }
    }
}

extension DeviceMonitoringService: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        DeviceStateManager.shared.updateLocation(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        )
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("[MonitoringService] Location error: \(error.localizedDescription)")
    }
}
