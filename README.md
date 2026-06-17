#  BackupsVault & Productivity Tracker

A visually polished, offline-first personal dashboard, goal tracker, and secure notes vault built for Android. **BackupsVault** combines a state-of-the-art Material 3 workspace with secure local persistence, deep dashboard analytics, and adaptive AI suggestions to organize your daily activities into clean **Areas of Life**.

---

##  Key Features

###  Robust Data Backup & Portability
*   **Local JSON Export/Import:** Seamlessly export all user profiles, custom categories, list items, completion timestamps, and rich document snippets to flat JSON backup files.
*   **Instant Sandbox Saves:** One-click instant backup and restore within your device's private sandboxed app cache for quick rollback and recovery.

### Smarter Organizing (Real-Time AI Categorization)
*   **LLM & Local Area Suggestions:** Dynamically inspects newly created tasks, ideas, or files, using an offline heuristic evaluator or online Gemini models to categorize them instantly into **Health**, **Career**, **Personal Growth**, **Leisure**, **Finance**, or **General**.
*   **Interactive AI Builder:** Trigger instantaneous single-tap "AI Suggest" actions from dialogs or view live recommendations inside the Document Vault.

###  Real-Time Analytics Workspace
*   **Dynamic Interactive Gauges:** Customize your Target Completion Rates on the fly using responsive Material 3 sliders.
*   **Area of Life Sorting & Filters:** Tailor your visual charts and rate cards through quick category switches (e.g., filter analytics strictly for *Career* or *Health*).

###  Encrypted Storage Vault & Notebook
*   **Attachments & Clippable Snippets:** Save formatting instructions, code pieces, long-form journals, or local lists.
*   **System Tags:** Visually labels notebooks by document formats, complete with dynamic localized timestamps.

###  Hybrid Copilot Hub
*   **Flexible AI Configurations:** Connect to the online **Gemini-3.5-Flash** engine, run offline models with **Ollama servers**, or use the built-in lightweight local heuristic pattern runner.

---

##  Built With

*   **Language:** [Kotlin](https://kotlinlang.org/) (100%)
*   **UI Architecture:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (declarative modern toolset)
*   **Core Toolkit:** Material Design 3 (M3) components with adaptive edge-to-edge screens.
*   **Database engine:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) (local SQLite wrapper with KSP compile-time checks)
*   **Reactive Flow:** Kotlin Coroutines and StateFlow for lightweight, non-blocking UI changes.
*   **Serialization:** System-level parsing via clean native JSON serializers.

---

##  Getting Started

### Prerequisites
*   Android Studio Ladybug (2024.2.1) or newer.
*   Android SDK Platform 34 or higher.

### Run the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/BackupsVault.git
