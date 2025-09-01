//
//  ContentView.swift
//  YOLOv11-Object-Detector-iOS-CoreML
//
//  Created by Surendra Maran on 25/06/25.
//

import SwiftUI

struct ContentView: View {
    let cameraService = CameraService()
    @Bindable private var detector: Detector
    
    init() {
        self.detector = Detector(modelName: Constants.modelName)
    }
    
    var body: some View {
        ZStack {
            CameraView(cameraService: cameraService) {buffer in
                detector.detectObjects(pixelBuffer: buffer)
            }
            
            GeometryReader { geometry in
                ForEach(detector.detectedObjects) { object in
                    BoundingBoxView(object: object, parentSize: geometry.size)
                }
            }
            VStack {
                Spacer()
                
                HStack {
                    Text(String(format: "Interface time: %.1f ms", detector.interfaceTime))
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding()
                        .background(Color.black.opacity(0.6))
                        .cornerRadius(8)
                    
                    Spacer()
                    
                    Link(destination: URL(string: "https://www.linkedin.com/in/surendra-maran/")!) {
                        Image("linkedin")
                            .resizable()
                            .frame(width: 36, height: 36)
                            .foregroundColor(.blue)
                            .padding(12)
                            .background(.white)
                            .clipShape(Circle())
                            .shadow(radius: 4)
                    }
                        
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                
            }
        }.ignoresSafeArea()
    }
}

#Preview {
    ContentView()
}


