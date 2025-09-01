//
//  ViewModel.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 31/08/25.
//

import CoreML
import AVFoundation
import Vision

@Observable @MainActor
final class Detector {
    private let visionModel: VNCoreMLModel?
    
    var detectedObjects : [BoundingBox] = []
    var interfaceTime: Double = 0.0
    
    var pixelsWide = 0
    var pixelsHigh = 0
    var labels: [String] = []
    
    let confidenceThreshold = 0.35
    
    init(modelName: String) {
        do {
            self.visionModel = try Utils.loadModel(named: modelName)
        } catch {
            self.visionModel = nil
            fatalError("❌ Failed to load model: \(error)")
        }
    }

    func detectObjects(pixelBuffer: CVPixelBuffer) {
        Task {
            let startTime = CFAbsoluteTimeGetCurrent()
            
            guard let visionModel = self.visionModel else { return }

            let request = VNCoreMLRequest(model: visionModel) { [weak self] request, error in
                if let results = request.results as? [VNRecognizedObjectObservation] {
                    let detectedData = results.compactMap { observation -> BoundingBox? in
                        let confidence = observation.confidence
                        if confidence < 0.75 {
                            return nil
                        }
                        guard let topLabel = observation.labels.first else { return nil }
                        return BoundingBox(
                            label: topLabel.identifier,
                            boundingBox: observation.boundingBox,
                            confidence: confidence
                        )
                    }
                    DispatchQueue.main.async {
                        self?.detectedObjects = detectedData
                        self?.interfaceTime = (CFAbsoluteTimeGetCurrent() - startTime) * 1000.0
                    }
                }
            }
            
            request.imageCropAndScaleOption = .scaleFit
            let handler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, options: [:])
            try? handler.perform([request])
        }
    }
}
