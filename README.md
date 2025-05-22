# PomodoroTimer

A feature-rich command-line Pomodoro Timer application built in Java that helps you manage your work sessions effectively using the Pomodoro Technique.

## Features

- 🎯 Customizable work session duration
- ⏰ Automatic break scheduling (short and long breaks)
- 🔊 Sound notifications for session transitions
- ⌨️ Interactive command-line interface with tab completion
- 📊 Session logging and analytics
- ⏸️ Pause, resume, and skip functionality
- 📝 Status tracking and session history

## Commands

- `pause` - Pause the current timer
- `resume` - Resume a paused timer
- `stop` - Stop the timer completely
- `status` - Show current timer status
- `skip` - Skip to the next session
- `help` - Show available commands

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven

### Installation

1. Clone the repository:
```bash
git clone https://github.com/GCremez/PomodoroTimer.git
cd PomodoroTimer
```

2. Build the project:
```bash
mvn clean package
```

3. Run the application:
```bash
java -jar target/PomodoroTimer.jar
```

## Usage

1. Start the application
2. Enter your desired work session duration in minutes
3. Use the available commands to control your sessions
4. The timer will automatically handle breaks between work sessions

## Features in Detail

### Sound Notifications
- Work session completion notification
- Break session completion notification

### Session Analytics
- Tracks total focus time
- Logs session history to JSON format
- Maintains statistics of your work patterns

### Break Schedule
- Short breaks (5 minutes) after regular sessions
- Long breaks (15 minutes) after every 4 work sessions

## Contributing

Feel free to submit issues and enhancement requests!