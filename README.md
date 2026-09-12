# AURA AI

**AURA AI** is a mobile-first AI personal assistant for Android, powered by OpenAI GPT.  
It understands natural language and performs real actions on your phone.

---

## Phases & Roadmap

This project will be developed and rolled out across clear phases to ensure a stable, secure, and user-friendly product. Below is the proposed set of phases and what each phase includes.

- Phase 0 — Proof of Concept (Completed)
  - Basic chat UI and integration with OpenAI API
  - Parse simple JSON actions (make_call, send_sms, set_reminder, launch_app)
  - Local storage for conversation history (Room)
  - Runtime permission handling for core features

- Phase 1 — Private Beta
  - Improve action parsing and error handling
  - Contact resolution and fallback flows when contacts not found
  - Add tests for repository and action parsing logic
  - Basic onboarding and privacy notice for users
  - Limit beta to invited testers and gather telemetry (opt-in)

- Phase 2 — Public Beta
  - Robust permission UX (granular explanations, graceful degradation)
  - Improved voice interaction (hotword, better noise handling)
  - Memory system improvements (context windows, summaries)
  - Analytics and crash reporting (GDPR/consent compliant)
  - Support for multiple assistant personalities (optional)

- Phase 3 — Production Release
  - Performance and battery optimizations
  - Security review and data handling audit
  - Accessibility improvements (TalkBack, large fonts)
  - App store preparation (screenshots, privacy policy)
  - Launch to public and staged rollouts

- Phase 4 — Post-launch Growth & Features
  - Multi-lingual support and localized models/prompts
  - Integrations (calendar providers, messaging apps)
  - Plugin/skill system for third-party actions
  - Offline-first capabilities for basic actions
  - Continuous model prompt tuning and monitoring

Rollout plan
- Start with internal testing (Phase 0 -> Phase 1 alpha testers).
- Collect feedback and crash reports; prioritize P1 issues.
- Expand to public beta with staged rollouts (Phase 2).
- Finalize security and store requirements; perform production release (Phase 3).

Success metrics
- Task completion rate for actions (calls, SMS, reminders)
- False-positive/incorrect-action rate
- User retention and DAU/MAU for beta testers
- Crash-free sessions and permission grant rates

---

## Features

| Feature | Status |
|---|---|
| 💬 Voice & text conversation with AI | ✅ |
| 📞 Make phone calls | ✅ |
| 💬 Send SMS messages | ✅ |
| ⏰ Set reminders and calendar events | ✅ |
| 📱 Open and control apps | ✅ |
| 🧠 Context-aware memory system | ✅ |

---

## Tech Stack

- **Platform**: Android (API 26+)
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **AI**: OpenAI GPT-4o-mini via REST API
- **Architecture**: MVVM + Clean Architecture (Use Cases)
- **DI**: Hilt
- **Database**: Room (conversation history + memory)
- **Networking**: Retrofit + OkHttp + Moshi
- **Async**: Kotlin Coroutines + Flow
- **Voice**: Android SpeechRecognizer

---

## Project Structure

```
app/src/main/java/com/aura/ai/
├── AuraApplication.kt          # Hilt application entry point
├── MainActivity.kt
├── data/
│   ├── database/               # Room DB (conversations + memory)
│   ├── model/                  # ChatMessage, AuraAction
│   ├── remote/                 # OpenAI Retrofit service + DTOs
│   └── repository/             # ConversationRepository, MemoryRepository
├── di/
│   └── AppModule.kt            # Hilt dependency graph
├── domain/
│   ├── action/
│   │   └── ActionParser.kt     # Parses AI responses into actions
│   └── usecase/                # MakeCall, SendSms, SetReminder, LaunchApp, …
├── ui/
│   ├── components/             # MessageBubble, InputBar, TypingIndicator
│   ├── screen/                 # MainScreen, ChatScreen
│   ├── theme/                  # Color, Type, Theme
│   └── viewmodel/
│       └── AuraViewModel.kt
└── util/
    ├── ReminderReceiver.kt     # BroadcastReceiver for alarm notifications
    └── VoiceHelper.kt          # SpeechRecognizer Flow wrapper
```

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/ferreiraeshawn8209-app/aura-ai.git
cd aura-ai
```

### 2. Configure Ollama

AURA connects to an Ollama-compatible server. Create (or edit) `local.properties` in the project root when you need to override the defaults:

```properties
OLLAMA_BASE_URL=http://10.0.2.2:11434/
OLLAMA_MODEL=llama3.2
```

The default base URL targets Ollama running on the host from an Android emulator. Use your machine's reachable address when running on a physical device.

> ⚠️ **Never commit `local.properties`** — it is already in `.gitignore`.

### 3. Build and run

Open the project in Android Studio (Hedgehog or newer) and run on a device or emulator with API 26+.

```bash
./gradlew assembleDebug
```

---

## How AURA Performs Actions

AURA uses a structured JSON convention to signal device actions.  
When the AI decides an action is needed it returns a response like:

```json
{
  "action": "make_call",
  "contact": "Mom",
  "reply": "Calling Mom now!"
}
```

Supported actions:

| `action` | Required fields | Description |
|---|---|---|
| `make_call` | `contact` | Dial a contact |
| `send_sms` | `contact`, `message` | Send a text message |
| `set_reminder` | `title`, `datetime` (yyyy-MM-dd HH:mm) | Schedule a notification |
| `launch_app` | `app_name` | Open an installed app |

---

## Permissions

AURA requests the following permissions at runtime:

| Permission | Purpose |
|---|---|
| `CALL_PHONE` | Make phone calls |
| `SEND_SMS` | Send text messages |
| `READ_CONTACTS` | Resolve contact names to numbers |
| `READ/WRITE_CALENDAR` | Create calendar events |
| `RECORD_AUDIO` | Voice input |
| `POST_NOTIFICATIONS` | Reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Precise reminder timing |

All permissions are optional — AURA degrades gracefully when denied.

---

## License

MIT — see [LICENSE](LICENSE).
