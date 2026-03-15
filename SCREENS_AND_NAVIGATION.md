# UniMind – Screens and Navigation

## Screens (Activities) – Up to All Set

| # | Activity | Purpose |
|---|----------|--------|
| 1 | **SplashActivity** | Launch screen (LAUNCHER) |
| 2 | **WelcomeActivity** | Welcome / intro |
| 3 | **LoginCheckActivity** | Check if user needs to sign in or sign up |
| 4 | **SignupActivity** | User registration |
| 5 | **SignInActivity** | User sign in |
| 6 | **ForgotPasswordActivity** | Password reset (from SignIn) |
| 7 | **ProfileSetupActivity** | Set up user profile |
| 8 | **DomainSelectionActivity** | Select domains |
| 9 | **GoalsActivity** | Set goals |
| 10 | **AiPersonalityActivity** | Choose AI personality |
| 11 | **PermissionsActivity** | App permissions |
| 12 | **ChatIntroActivity** | Chat intro |
| 13 | **TrackProgressActivity** | Track progress |
| 14 | **SuggestionsActivity** | Suggestions |
| 15 | **AllSetActivity** | “All set” summary – **last screen** |

**Total: 15 activities.** App flow ends at All Set; “Continue” finishes the activity.

---

## Navigation Flow

```
SplashActivity
    → WelcomeActivity
        → LoginCheckActivity
            → SignupActivity  ←→  SignInActivity
            |                       ├→ ForgotPasswordActivity
            |                       └→ ProfileSetupActivity
            └→ SignInActivity
                    └→ ProfileSetupActivity
                            → DomainSelectionActivity
                                    → GoalsActivity
                                            → AiPersonalityActivity
                                                    → PermissionsActivity
                                                            → ChatIntroActivity
                                                                    → TrackProgressActivity
                                                                            → SuggestionsActivity
                                                                                    → AllSetActivity
                                                                                            → [Continue] finish()
```

- **LAUNCHER:** **SplashActivity** (app entry point).
- **Last screen:** **AllSetActivity**. Tapping “Continue” calls `finish()` (returns to previous screen).
