import Foundation

class RemotePolicyManager {
    
    static let shared = RemotePolicyManager()
    
    private let serverURL = "https://your-mdm-server.com/api"
    private let session: URLSession
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 60
        session = URLSession(configuration: config)
    }
    
    struct PolicyResponse: Codable {
        let success: Bool
        let policies: [Policy]?
        let message: String?
    }
    
    struct Policy: Codable {
        let id: String
        let name: String
        let type: String
        let value: String
        let enforced: Bool
    }
    
    func fetchPolicies(completion: @escaping ([Policy]?) -> Void) {
        guard let url = URL(string: "\(serverURL)/policies") else {
            completion(nil)
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue(getDeviceId(), forHTTPHeaderField: "X-Device-ID")
        
        session.dataTask(with: request) { data, response, error in
            guard let data = data, error == nil else {
                print("[RemotePolicyManager] Fetch error: \(error?.localizedDescription ?? "Unknown")")
                completion(nil)
                return
            }
            
            do {
                let response = try JSONDecoder().decode(PolicyResponse.self, from: data)
                completion(response.policies)
            } catch {
                print("[RemotePolicyManager] Decode error: \(error)")
                completion(nil)
            }
        }.resume()
    }
    
    func reportDeviceState(_ state: DeviceStateManager.DeviceState, completion: @escaping (Bool) -> Void) {
        guard let url = URL(string: "\(serverURL)/device/state") else {
            completion(false)
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue(getDeviceId(), forHTTPHeaderField: "X-Device-ID")
        
        do {
            let encoder = JSONEncoder()
            encoder.dateEncodingStrategy = .iso8601
            request.httpBody = try encoder.encode(state)
        } catch {
            print("[RemotePolicyManager] Encode error: \(error)")
            completion(false)
            return
        }
        
        session.dataTask(with: request) { data, response, error in
            if let httpResponse = response as? HTTPURLResponse {
                completion(httpResponse.statusCode == 200)
            } else {
                completion(false)
            }
        }.resume()
    }
    
    func sendCommand(_ command: String, parameters: [String: Any]?, completion: @escaping (Bool) -> Void) {
        guard let url = URL(string: "\(serverURL)/device/command") else {
            completion(false)
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue(getDeviceId(), forHTTPHeaderField: "X-Device-ID")
        
        var body: [String: Any] = ["command": command]
        if let params = parameters {
            body["parameters"] = params
        }
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
        } catch {
            completion(false)
            return
        }
        
        session.dataTask(with: request) { data, response, error in
            if let httpResponse = response as? HTTPURLResponse {
                completion(httpResponse.statusCode == 200)
            } else {
                completion(false)
            }
        }.resume()
    }
    
    private func getDeviceId() -> String {
        return UIDevice.current.identifierForVendor?.uuidString ?? ""
    }
}

import UIKit
