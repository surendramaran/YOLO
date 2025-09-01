//
//  CameraService.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 31/08/25.
//

import AVFoundation
import SwiftUI
import CoreML

class CameraService: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
    
    var session: AVCaptureSession?
    let previewLayer = AVCaptureVideoPreviewLayer()
    private var videoOutput = AVCaptureVideoDataOutput()
    
    var frameHandler: ((CVPixelBuffer) -> Void)?
    
    func start(frameHandler: @escaping (CVPixelBuffer) -> Void, completion: @escaping (Error?) -> Void) {
        self.frameHandler = frameHandler
        checkPermissions(completion: completion)
    }
    
    private func checkPermissions(completion: @escaping (Error?) -> Void) {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                guard granted else { return }
                DispatchQueue.main.async {
                    self?.startCamera(completion: completion)
                }
            }
        case .restricted, .denied:
            completion(
                NSError(
                    domain: "CameraService",
                    code: -2,
                    userInfo: [NSLocalizedDescriptionKey: "Camera access denied"]
                )
            )
        case .authorized:
            startCamera(completion: completion)
        @unknown default:
            break
        }
    }
    
    private func startCamera(completion: @escaping (Error?) -> Void) {
        let session = AVCaptureSession()
        session.sessionPreset = .photo
        
        guard let device = AVCaptureDevice.default(for: .video) else {
            completion(
                NSError(
                    domain: "CameraService",
                    code: -1,
                    userInfo: [NSLocalizedDescriptionKey: "No camera available"]
                )
            )
            return
        }
        
        do {
            let input = try AVCaptureDeviceInput(device: device)
            if session.canAddInput(input) {
                session.addInput(input)
            }
            
            videoOutput.videoSettings = [
                kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA
            ]
            videoOutput.setSampleBufferDelegate(self, queue: DispatchQueue(label: "camera.frame.queue"))
            videoOutput.alwaysDiscardsLateVideoFrames = true
            
            if session.canAddOutput(videoOutput) {
                session.addOutput(videoOutput)
                if let connection = videoOutput.connection(with: .video) {
                    if connection.isVideoRotationAngleSupported(90) {
                        connection.videoRotationAngle = 90
                    }
                }
            }
            
            previewLayer.videoGravity = .resizeAspect
            previewLayer.session = session
            
            
            DispatchQueue.global(qos: .userInitiated).async {
                session.startRunning()
            }

            self.session = session
        } catch {
            completion(error)
        }
    }
    

    func stop() {
        guard let session = session else { return }
        
        session.stopRunning()
        if let device = AVCaptureDevice.default(for: .video), device.hasTorch {
            do {
                try device.lockForConfiguration()
                device.torchMode = .off
                device.unlockForConfiguration()
            } catch {
                print("Error disabling torch: \(error)")
            }
        }
        self.session = nil
    }
    
    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        
        let currentOrientation = UIDevice.current.orientation
            
        if let correctedPixelBuffer = applyRotation(pixelBuffer, orientation: currentOrientation) {
            frameHandler?(correctedPixelBuffer)
        }
    }
    
    func applyRotation(_ pixelBuffer: CVPixelBuffer, orientation: UIDeviceOrientation) -> CVPixelBuffer? {
        let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
        
        var transform = CGAffineTransform.identity
        switch orientation {
        case .portrait:
            transform = .identity
        case .portraitUpsideDown:
            transform = CGAffineTransform(rotationAngle: .pi)
        case .landscapeLeft:
            transform = CGAffineTransform(rotationAngle: .pi / 2)
        case .landscapeRight:
            transform = CGAffineTransform(rotationAngle: -.pi / 2)
        default:
            transform = .identity
        }
        
        let rotatedImage = ciImage.transformed(by: transform)
        
        let context = CIContext()
        var rotatedPixelBuffer: CVPixelBuffer?
        
        let result = CVPixelBufferCreate(kCFAllocatorDefault,
                                         CVPixelBufferGetWidth(pixelBuffer),
                                         CVPixelBufferGetHeight(pixelBuffer),
                                         kCVPixelFormatType_32BGRA,
                                         nil,
                                         &rotatedPixelBuffer)
        
        if result == kCVReturnSuccess, let rotatedBuffer = rotatedPixelBuffer {
            context.render(rotatedImage, to: rotatedBuffer)
            return rotatedBuffer
        } else {
            return nil
        }
    }
    
    private func updatePreviewLayerOrientation() {
        guard let connection = previewLayer.connection else { return }

        switch UIDevice.current.orientation {
        case .portrait:
            connection.videoRotationAngle = 0
        case .portraitUpsideDown:
            connection.videoRotationAngle = 180
        case .landscapeLeft:
            connection.videoRotationAngle = 90
        case .landscapeRight:
            connection.videoRotationAngle = 270
        default:
            break
        }
    }


    
}
