//
//  Utils.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 31/08/25.
//

import SwiftUI
import Vision

struct Utils {
    static func loadModel(named modelName: String) throws -> VNCoreMLModel {
        guard let url = Bundle.main.url(forResource: modelName, withExtension: "mlmodelc") else {
            throw NSError(domain: "ModelLoader", code: -1, userInfo: [NSLocalizedDescriptionKey: "Model \(modelName) not found"])
        }
        let coreMLModel = try MLModel(contentsOf: url)
        return try VNCoreMLModel(for: coreMLModel)
    }
    
    
    static func computeBoundingBox(for object: BoundingBox, in size: CGSize) -> CGRect {
        let viewWidth = size.width
        let viewHeight = size.height
        let videoAspect: CGFloat = 3.0 / 4.0

        let scaleWidth: CGFloat
        let scaleHeight: CGFloat
        let xOffset: CGFloat
        let yOffset: CGFloat

        if viewWidth / viewHeight > videoAspect {
            scaleHeight = viewHeight
            scaleWidth = scaleHeight * videoAspect
            xOffset = (viewWidth - scaleWidth) / 2
            yOffset = 0
        } else {
            scaleWidth = viewWidth
            scaleHeight = scaleWidth / videoAspect
            xOffset = 0
            yOffset = (viewHeight - scaleHeight) / 2
        }

        let boundingBox = object.boundingBox
        return CGRect(
            x: xOffset + boundingBox.origin.x * scaleWidth,
            y: yOffset + (1 - boundingBox.origin.y - boundingBox.height) * scaleHeight,
            width: boundingBox.width * scaleWidth,
            height: boundingBox.height * scaleHeight
        )
    }
    
    
    func resizePixelBuffer(_ pixelBuffer: CVPixelBuffer, width: Int, height: Int) -> CVPixelBuffer? {
        var resized: CVPixelBuffer?
        let attrs = [
            kCVPixelBufferCGImageCompatibilityKey: true,
            kCVPixelBufferCGBitmapContextCompatibilityKey: true
        ] as CFDictionary

        let status = CVPixelBufferCreate(
            kCFAllocatorDefault,
            width,
            height,
            kCVPixelFormatType_32BGRA,
            attrs,
            &resized
        )
        guard status == kCVReturnSuccess, let output = resized else {
            return nil
        }

        let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
        let context = CIContext()
        let sx = CGFloat(width) / ciImage.extent.width
        let sy = CGFloat(height) / ciImage.extent.height
        let scaled = ciImage.transformed(by: CGAffineTransform(scaleX: sx, y: sy))
        context.render(scaled, to: output)
        return output
    }

}
