# HydroMate 💧

A beautiful, modern Android app for tracking daily water intake with gamification elements.

## Features ✨

### Core Functionality
- **Water Intake Tracking**: Easy logging with customizable quick-add buttons
- **Daily Goals**: Set and track personalized hydration targets
- **Character System**: Animated buddy that reacts to your progress
- **Statistics**: Comprehensive weekly and monthly analytics
- **History Calendar**: Visual progress tracking over time
- **Smart Notifications**: Customizable reminders to stay hydrated

### Design & UX
- **Material Design 3**: Modern, accessible interface
- **Dark/Light Theme**: Automatic system theme support
- **Smooth Animations**: Delightful micro-interactions
- **Haptic Feedback**: Enhanced user experience
- **Responsive Design**: Works great on all screen sizes

### Technical Excellence
- **Clean Architecture**: Separation of concerns with Domain/Data/Presentation layers
- **MVI Pattern**: Predictable state management
- **Jetpack Compose**: Modern declarative UI
- **Room Database**: Local data persistence
- **Hilt Dependency Injection**: Modular and testable code
- **Kotlin Coroutines**: Reactive programming with Flow

## Architecture 🏗️

```
┌─────────────────────┐
│    Presentation     │
│  Compose UI + MVI   │
├─────────────────────┤
│       Domain        │
│ UseCases + Entities │
├─────────────────────┤
│        Data         │
│  Room + Repository  │
└─────────────────────┘
```

### Key Components
- **Entities**: Core business models (WaterEntry, UserSettings, etc.)
- **Use Cases**: Business logic operations
- **Repository Pattern**: Data access abstraction
- **ViewModels**: State management with MVI pattern
- **Compose UI**: Modern declarative interface

## Tech Stack 🛠️

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture + MVI
- **DI**: Hilt
- **Database**: Room
- **Async**: Coroutines + Flow
- **Navigation**: Navigation Compose
- **Animations**: Compose Animations API

## Key Features Deep Dive 🔍

### Home Screen
- Real-time progress tracking with animated character
- Quick-add buttons for common amounts
- Beautiful wave-based progress indicator
- Today's entries with swipe-to-delete

### Statistics Screen
- Weekly overview with charts and metrics
- Achievement system with badges
- Streak tracking and consistency metrics
- Interactive data visualization

### History Screen
- Calendar view with color-coded progress
- Detailed daily breakdowns
- Monthly summaries and trends
- Tap any day to see detailed entries

### Settings Screen
- Personalized daily goals (500ml - 5L)
- Character selection (5 cute options)
- Smart notification scheduling
- Customizable quick-add amounts

### Character System
5 adorable character states based on progress:
- 🥺 **Thirsty** (0-25%): Needs water urgently
- ⏳ **Slightly Thirsty** (25-50%): Getting started
- 👍 **Content** (50-75%): Making good progress
- 😊 **Happy** (75-100%): Almost there!
- 🎉 **Very Happy** (100%+): Goal achieved!

### Development Guidelines
- Follow Clean Architecture principles
- Write comprehensive tests
- Use meaningful commit messages
- Maintain code documentation
- Follow Kotlin coding conventions

## License 📄

This project is for educational purposes and demonstrates modern Android development practices.

## Contributing 🤝

Feel free to:
- Add new features
- Improve existing functionality
- Fix bugs
- Enhance UI/UX
- Add tests
