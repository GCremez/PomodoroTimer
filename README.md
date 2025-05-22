# Pomodoro Timer

A feature-rich Pomodoro Timer application built in Java with modern software engineering practices.

## Features

- Configurable work and break durations
- Sound notifications for session transitions
- Session logging and analytics
- Colorful terminal interface
- Command-line controls
- Configuration system using Typesafe Config
- Comprehensive logging with SLF4J and Logback
- Proper exception handling and input validation

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/PomodoroTimer-1.0-SNAPSHOT.jar
```

## Configuration

The application uses the following configuration hierarchy:
1. Default settings in `src/main/resources/reference.conf`
2. Optional user settings in `application.conf`

### Example Configuration

```hocon
pomodoro {
    timer {
        default-work-duration = 25    # minutes
        short-break-duration = 5      # minutes
        long-break-duration = 15      # minutes
        sessions-before-long-break = 4
    }
    sound {
        enabled = true
        volume = 1.0
        work-sound-file = "sounds/work.wav"
        break-sound-file = "sounds/break.wav"
    }
}
```

## Commands

- `pause` - Pause the current timer
- `resume` - Resume a paused timer
- `stop` - Stop the timer completely
- `status` - Show current timer status
- `skip` - Skip to the next session
- `help` - Show help message

## Logging

Logs are written to:
- Console (INFO level and above)
- `logs/pomodoro.log` (with daily rolling)

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.