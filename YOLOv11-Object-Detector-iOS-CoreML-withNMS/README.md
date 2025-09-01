# YOLOv11 Object Detection iOS Application

## Description
This iOS application is designed to perform live object detection using the YOLOv11 machine learning model.  
YOLOv11 (You Only Look Once version 11) is known for its real-time object detection capabilities, and this app brings that functionality to iOS devices with SwiftUI, AVFoundation, and Core ML.

## Getting Started
To use this repository for any custom YOLOv11 object detection model, follow these steps:

### Export Your Model
Convert your YOLOv11 PyTorch model to Core ML format with Non-Maximum Suppression enabled:

```bash
yolo export model=<YOUR_MODEL> format=coreml nms=True
```

### Clone the Project
Clone this project to your local machine and open it in Xcode.

### Add Model to Project
Drag and drop your `.mlpackage` file into the Xcode project. Ensure the model is added to the app target so it is bundled in the app. Xcode will auto-generate a Swift class for your model.

### Update Model Reference
Open `Constant.swift`. Replace the sample model reference with your custom `.mlpackage`.

### Build and Run
Connect your iPhone (required for live camera testing). Select your device as the run destination in Xcode. Build and run the app to start live object detection.

## Contributing
Contributions are welcome! If you want to contribute to this project, feel free to fork the repository and submit a pull request with your changes.

### Contact
For any questions or feedback, feel free to  an [issue](https://github.com/surendramaran/YOLO/issues/new) in the repository or message on me my [LinkedIn](https://www.linkedin.com/in/surendra-maran/).

### Support
If you find this project helpful and want to support its development, consider becoming a patron on [Patreon](https://www.patreon.com/SurendraMaran). Your support will help in maintaining and improving the project. Thank you!.
