//
//  BoundingBox.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 31/08/25.
//

import Foundation

struct BoundingBox: Identifiable {
    var id = UUID()
    let label: String
    let boundingBox: CGRect
    let confidence: Float
}
