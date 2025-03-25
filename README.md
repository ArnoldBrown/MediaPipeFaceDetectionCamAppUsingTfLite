# DrivingTestAI

### Overview

DrivingTestAI is an Android camera app that continuously captures images through both front and back cameras simultaneously. It requires a mobile device that supports concurrent front and back camera usage to leverage its camera hardware fully.

- **Front Camera**: Detects a FACE and SEAT BELT, drawing bounding boxes and confidence levels for the detected objects.
- **Back Camera**: Detects TRAFFIC LIGHTS (green, red, yellow) and ZEBRA CROSSINGS.

The app uses the model file `best_float_6C_32.tflite`, which should be placed in the app's *assets* directory. The app is designed to run on a physical Android device for accurate detection.

### Features

- **Real-time detection**:
    - **Front Camera**: Detects faces and seat belts, drawing bounding boxes with confidence levels.
    - **Back Camera**: Detects traffic light colors and zebra crossings.
- **API Integration**: In-built Ktor server to manage the test lifecycle via API calls.
- **Face Matching**: Compares a live face with a base image provided when the test starts.
- **Image Preprocessing**: Handles device orientation and scaling before sending to the model.

### Ktor Server

The app includes an in-built Ktor server to handle API requests for:
- Starting and stopping the main test
- Starting and stopping the "H" and "8" track tests

#### API Endpoints

1. **`checkStatus`**:
    - Parameters:
        - `currentStatus` (`START`/`STOP`)
        - `baseImage` (Base64-encoded image of the user's face for comparison)
    - **START**: Starts the main driving test. The base image is stored and used to compare faces during the test.
    - **STOP**: Stops the current test.

2. **`checkHStatus`**:
    - Starts or stops the "H" track test. This can only be started if the main test is already running.

3. **`checkEtStatus`**:
    - Starts or stops the "8" track test. This can only be started if the main test is already running.

> Note: Only one track test (either "H" or "8") can be active at any given time.

For quick testing, there are three green buttons:
- **START**: Starts the main test.
- **H**: Starts the "H" track test.
- **8**: Starts the "8" track test.

### Camera and Detection Workflow

#### Front Camera Detection
The app focuses on detecting three classes:
- **0**: No seat belt
- **1**: Wearing a seat belt
- **2**: Face detected

Captured images are processed as follows:
1. **ImageProxy to Bitmap Conversion**: The `ImageProxy` is converted into a `Bitmap`.
2. **Rotation**: Images are rotated based on front/back camera and device orientation.
3. **Pre-scaling**: The image is scaled to match the input requirements of the model.

The app uses the sensor manager and accelerometer to detect device orientation. The orientation is used to adjust the image before preprocessing.

#### Image Preprocessing
- **`preprocessImage`**: Resizes images to 640x640 pixels, returning the preprocessed bitmap and scale factors.
- **`convertBitmapToByteBuffer`**: Converts the preprocessed `Bitmap` into a `ByteBuffer` suitable for model input.

#### Model Execution
- **`runModel`**: The TensorFlow Lite model processes the `inputBuffer` and returns scores, class IDs, and bounding boxes.

#### Non-Maximum Suppression (NMS)
- The NMS function minimizes overlapping bounding boxes based on predefined thresholds for detecting seat belts and no seat belts.

#### Detection and Bounding Box Drawing
- Bounding boxes are drawn around detected seat belts, no seat belts, and faces. If a face (Class 2) is detected, the app calls the `cropFaces` and `compareFaces` functions to verify face similarity, and the detection results are displayed.
- The final image, with bounding boxes, is displayed in the ImageView preview for FACE, FACE MATCHED, SEAT BELT (SB), and NO SEAT BELT (NSB).

#### Back Camera Detection
- The process is the same as for the front camera, but the model detects traffic lights (GREEN, RED, YELLOW) and zebra crossings. Bounding boxes are drawn accordingly.

### Face Comparison
- Face comparison is handled using the `arcface.tflite` model, which is located in the *assets* folder. When a face is detected, it is compared to the base image provided at the start of the test.

### Additional Notes
- The app supports real-time detection and comparison of faces, seat belts, traffic lights, and zebra crossings.
- Both front and back cameras must be functional and capable of simultaneous usage for optimal performance.

### Requirements
- A physical Android device that supports concurrent front and back camera capture.
- TensorFlow Lite models (`best_float_6C_32.tflite` for detection and `arcface.tflite` for face comparison) must be placed in the *assets* directory.
- The app relies on the Ktor server to handle API requests for starting and stopping tests.

### License
This project is licensed under the MIT License. See the LICENSE file for details.
