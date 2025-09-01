//
//  CameraView.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 31/08/25.
//

import SwiftUI
import AVFoundation

struct CameraView: UIViewControllerRepresentable {
    typealias UIViewControllerType = UIViewController
    
    let cameraService: CameraService
    let didOutputFrame: (CVPixelBuffer) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        cameraService.start(frameHandler: { buffer in
            didOutputFrame(buffer)
        }, completion: { err in
            if let err = err {
                print("Camera error:", err)
            }
        })
        
        let viewController = UIViewController()
        viewController.view.backgroundColor = .black
        viewController.view.layer.addSublayer(cameraService.previewLayer)
        cameraService.previewLayer.frame = viewController.view.bounds
        
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
//        cameraService.previewLayer.frame = uiViewController.view.bounds
    }
}
