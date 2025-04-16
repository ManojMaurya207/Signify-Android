# ✨ Signify – Bridging Communication through Sign Language

**Signify** is an inclusive Android app designed to help users learn, practice, and translate sign language. It integrates advanced technologies like real-time gesture recognition, speech synthesis, and Firebase to create an accessible and educational experience for everyone.

---

## 📱 Features

### 🔤 Sign Language Translation
- **Sign to Text**: Real-time translation of sign gestures to text using the device camera and MediaPipe.
- **Text to Sign**: Converts typed text into visual sign representations.
- **Speech Output**: Text is spoken aloud using Text-to-Speech with customizable voice selection.

### 🎥 Learn Section
- **Video Lessons**: Watch curated YouTube videos embedded directly in the app.
- **Interactive Articles**: Educational content to enhance sign language knowledge.
- **Trending Courses**: Cards showcasing popular learning paths.

### 🧠 Duolingo-style Lessons *(Upcoming)*
- Guess the correct sign text from GIFs or images.
- Track your score and progress per lesson.

### ⚙️ Settings & Personalization
- **Dark Mode**: Toggle between light and dark themes.
- **Voice Selection**: Choose your preferred Text-to-Speech voice.
- **Ratings & Feedback**: Rate the app and share thoughts.
- **Profile Management**: Login/logout with Firebase Auth.

---

## 🚀 Technologies Used

### 👁️‍🗨️ Frontend
- **Jetpack Compose** – Modern, declarative UI toolkit.
- **Material Design 3** – Consistent and sleek UI components.
- **CameraX** – Real-time camera preview and capture.

### 🧠 Machine Learning
- **MediaPipe HandLandmarker** – Detects hand landmarks for gesture recognition.
- **GestureRecognizer** – Maps hand gestures to meaningful text.
- **Flask API (Planned)** – Custom model integration for sign-to-text conversion.

### ☁️ Backend & Database
- **Firebase Authentication** – Secure user authentication.
- **Cloud Firestore** – Real-time storage for ratings, progress, and feedback.
- **Firestore Snapshots** – Live updates for user data like ratings and geofences.

---
