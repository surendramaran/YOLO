//
//  BoundingBoxView.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 31/08/25.
//

import SwiftUI

struct BoundingBoxView: View {
    let object: BoundingBox
    let parentSize: CGSize
    
    static let classColors: [String: Color] = [:]
    
    private func color(for label: String) -> Color {
        if let existing = BoundingBoxView.classColors[label] {
            return existing
        } else {
            let hash = abs(label.hashValue)
            let hue = Double((hash % 256)) / 255.0
            let color = Color(hue: hue, saturation: 0.8, brightness: 0.9)
            return color
        }
    }

    var body: some View {
        let rect = Utils.computeBoundingBox(for: object, in: parentSize)
        let boxColor = color(for: object.label)
        
        
        ZStack {
            Rectangle()
                .stroke(boxColor, lineWidth: 2)
                .frame(width: rect.width, height: rect.height)
                .position(x: rect.midX, y: rect.midY)
            
            Text(object.label + ": " + String(format: "%.0f%%", object.confidence * 100))
                .foregroundColor(.white)
                .padding(5)
                .background(boxColor.opacity(0.9))
                .cornerRadius(8)
                .position(x: rect.midX, y: rect.minY - 10)
        }
    }
}
