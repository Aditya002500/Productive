# Productivity Planner + Screenshot Intelligence App PRD

## Product overview
This product is an Android-first productivity application, with later iOS expansion, designed to unify planning, notes, timetable management, and AI-assisted screenshot organization in one mobile experience. Existing productivity leaders remain fragmented across tasks, notes, calendar, and habits, while newer screenshot-specific apps validate demand for OCR, AI categorization, summaries, tags, reminders, and natural-language retrieval, creating an opening for a product that connects capture directly to action.[cite:14][cite:15][cite:18]

The app should position itself as a planner for users who save information faster than they organize it. Instead of trying to replace every productivity category at once, the product should combine strong daily planning fundamentals with a differentiated screenshot pipeline that converts saved images into usable notes, reminders, and schedule items.[cite:15][cite:18][cite:20]

## Problem statement
Mobile users often split their productivity workflow across separate apps for tasks, notes, schedules, and screenshot storage. Screenshot organizer apps show that users increasingly want screenshots to be searchable, categorized, summarized, and reusable, but current products usually stop at organization rather than deeply integrating screenshots into planning workflows.[cite:14][cite:15][cite:16]

Students and busy professionals also rely on timetable-style planning, reminders, and lightweight notes for everyday execution. School planner apps commonly support timetable, assignments, reminders, exams, and widgets, which confirms persistent demand for schedule-centric workflows on mobile.[cite:17][cite:20][cite:22]

## Product vision
The product vision is to become the default mobile inbox for planned work and captured information. A user should be able to manage today’s schedule, write notes, save a screenshot, receive an AI-generated title and summary, edit the result, and convert that screenshot into a task, event, or note from one place.[cite:15][cite:18][cite:21]

The experience should feel faster and more focused than broad all-in-one workspace tools. The product should be local-first where possible, privacy-forward in how screenshot data is processed, and specialized for the mobile use case rather than copied from desktop productivity software.[cite:14][cite:15][cite:18]

## Target users
### Primary segments
- Students who manage lectures, assignments, exams, class timetables, and reference screenshots of announcements or study material.[cite:17][cite:20][cite:22]
- Young professionals and builders who track tasks, meetings, links, receipts, chats, and document screenshots for follow-up.[cite:14][cite:18][cite:21]
- Heavy screenshot savers who frequently lose information in the photo gallery and need search, categorization, and reminders.[cite:14][cite:15][cite:16]

### Secondary segments
- Researchers and knowledge workers who save articles, snippets, schedules, and code screenshots that later need to become structured notes or reminders.[cite:15][cite:18]
- Travel, shopping, and event users who need tickets, receipts, booking confirmations, and offers extracted from screenshots into organized collections.[cite:14][cite:18][cite:21]

## Positioning
The product should be positioned as a mobile planner with screenshot intelligence, not just another to-do list or generic note app. The clearest differentiation comes from combining timetable, tasks, and notes with screenshot OCR, AI summaries, smart categories, reminder generation, and natural-language search in a single workflow.[cite:14][cite:15][cite:18]

A strong positioning line is: “Plan your day, save what matters, and turn screenshots into action.” That message aligns with the market pattern in which screenshot apps emphasize organization and recall, while planner apps emphasize time and execution.[cite:15][cite:18][cite:20]

## Competitive landscape
This section names live competitors as of mid-2026 so positioning and roadmap decisions are grounded in the current market rather than the category in the abstract.

### Screenshot organizer competitors
- **Sorti** — cross-platform, AI auto-categorizes screenshots plus shared links, TikTok/Instagram saves, and recipes into one library, and keeps a live link back to the source post when the platform exposes one. Its semantic search ("that jacket I saw last week") is the closest existing analogue to this PRD's natural-language search requirement.[23]
- **MarkIt** — captures via a WhatsApp/Telegram bot rather than a native gallery importer, OCRs and auto-tags on receipt, and proxies the original image so social-CDN links don't expire. Free tier caps around 40 captures/month.[24]
- **Fabric** — positions itself as a general "save anything" workspace (screenshots, links, files) with a browser extension that preserves the source URL, which native mobile screenshot tools structurally cannot do.[24]
- **Captr (iOS)** — screenshot-to-task conversion with reminders and a "Catch Up" review surface; no Android support and requires a paid subscription, which is a gap this PRD's Android-first approach can exploit.[23]
- **PixelShot (Android)** — closest direct analogue: on-device categorization and summaries, with an explicit "text-only" cloud-processing pattern for AI summaries. User reviews cite two recurring complaints worth designing around: AI-generated tags/descriptions that cannot be edited, and no nested collections/groups.[27]
- **Google Pixel Screenshots / Apple Photos Live Text** — the "good enough for free" baseline built into the OS on flagship devices. Any paid competitor has to clearly beat this before a user will install a third app.[24]

### Student planner / timetable competitors
- **MyStudyLife** — free, handles rotating (A/B, Week 1/2) timetables, exam countdowns, and cross-device sync; widely cited as the default free choice, including a premium "AI Schedule Scan" that builds a timetable from a photo.[26][29]
- **myHomework** — homework/assignment tracking with a home-screen widget showing what's due; simplicity is the selling point over feature depth.[25]
- **Fhynix** — calendar-first, AI-assisted planner aimed at Indian students and parents specifically, with WhatsApp reminders and syncing of school/coaching-institute timetables — the most directly regional competitor.[28]
- **Notion / Todoist / Google Calendar** — flexible, non-student-specific tools students repurpose; strong ecosystems but no timetable primitives (rotating schedules, exam countdowns) out of the box.[26][29]

### Where the gap actually is
No competitor above combines a first-class academic timetable with a genuine AI screenshot-to-action pipeline; screenshot apps are drifting toward "save anything" breadth (Sorti, Fabric) while planner apps are drifting toward AI scheduling assistance (MyStudyLife's Schedule Scan, Fhynix's natural-language event creation) — but neither side has merged the two loops yet. That confirms the original positioning, with one adjustment: the bar for the screenshot side is no longer just OCR + category + summary (table stakes now), it's whether captured content stays linked to its source and whether categories/tags stay user-correctable, since both are explicit points of user complaint against the closest existing competitor.

## Product pillars
### 1. Daily execution
The app should provide a fast daily workflow with Today view, tasks, reminders, and timetable blocks. Successful student planner apps consistently center on day/week/month views, event tracking, alarms, and completion states.[cite:17][cite:20]

### 2. Knowledge capture
The app should support rapid capture through notes, quick add, and screenshot import. Screenshot organizer apps show that instant text extraction, categorization, and search materially improve recall and reduce scrolling through the gallery.[cite:14][cite:15][cite:16]

### 3. AI-assisted organization
AI should reduce manual work by suggesting categories, headings, tags, summaries, dates, and next actions. Current screenshot apps already use AI for categorization, summaries, text search, and smart tags, which validates this product direction.[cite:15][cite:18][cite:21]

### 4. Editable control
All AI outputs should remain editable, because OCR and summarization are probabilistic and users need trust. The system should always preserve the original screenshot and raw extracted text alongside editable metadata and structured fields.[cite:15][cite:18]

### 5. Privacy and local-first behavior
On-device OCR and local screenshot analysis should be used wherever feasible. Multiple screenshot apps explicitly market on-device processing and private handling of screenshot data, indicating this is a meaningful competitive factor rather than a technical detail.[cite:14][cite:15][cite:18]

## Core feature set
## Planning and productivity
### Today view
- Agenda for today with timeline blocks, current/upcoming tasks, overdue items, reminders, and quick capture.[cite:20]
- Priority strip for top 3 items.
- Auto-grouping by morning, afternoon, evening.
- Progress ring for completed tasks and events.

### Tasks
- Inbox, Today, Upcoming, Someday.
- Due dates, reminders, recurring tasks, priorities, subtasks, attachments, tags.
- Convert note or screenshot into task.
- Smart task suggestions from screenshot text such as deadlines or action phrases.[cite:14][cite:15]

### Timetable and calendar
- Class/work timetable with recurring weekly slots.
- Day, week, month views.
- Exams, meetings, assignment deadlines, and all-day events.[cite:17][cite:20]
- Color coding by subject, project, or workspace.
- Optional sync layer for Google Calendar and Apple Calendar in later releases.

### Notes
- Rich text or markdown-lite editor.
- Headings, bullet lists, checklists, quotes, code block support, inline image attachment.
- Note templates for lecture notes, meeting notes, weekly review, assignment planner.
- Pinning, tagging, linking to tasks/events/screenshots.

### Reminders
- Time-based and date-based reminders.
- Pre-event reminder presets.
- Reminder from screenshot or note.[cite:14][cite:16]
- Snooze, complete, reschedule, and quick actions from notification.

## Screenshot intelligence
### Screenshot ingestion
- Manual import from gallery.
- Auto-detect newly created screenshots with opt-in prompt.[cite:14][cite:15]
- Batch import and multi-select.
- Timeline view and grid view.[cite:18]
- Share-sheet / "send to app" target so links, forwarded chat messages, and images from other apps (browser, WhatsApp, Instagram) can be captured directly, not only screenshots. This widens the capture surface the same way competing tools now do with chat-app bots and browser extensions, and it sidesteps most of the Android photo-library permission constraints described in Legal, privacy, and platform compliance below.[23][24]
- Voice quick-capture: a press-and-hold mic action that transcribes a spoken note into a task, reminder, or note without opening the full editor, in service of the sub-3-second capture goal below.

### OCR and parsing
- Extract all text from screenshot.
- Detect language.
- Identify entities such as dates, times, URLs, phone numbers, codes, prices, addresses, and names.[cite:21]
- Preserve raw OCR text for audit and fallback search.

### AI categorization
The default category system should include:
- Documents
- Timetable / schedule
- Receipts / bills
- Tickets / reservations
- Shopping / product
- Chats / social
- Articles / web info
- Notes / ideas
- Code / technical
- Media / entertainment
- Places / travel
- Other

Competing screenshot apps already categorize screenshots into bills, tickets, shopping, documents, receipts, codes, articles, chats, events, places, and ideas, which supports a broad but practical starting taxonomy.[cite:14][cite:18][cite:21]

### AI summary and heading generation
- Generate a one-line title from screenshot content.[cite:15][cite:18]
- Generate a short summary for text-heavy screenshots.[cite:15][cite:18]
- Extract key bullets for long screenshots.
- Generate “why this may matter” hints such as deadline, booking, class change, or coupon.
- Use fallback behavior for low-confidence OCR.

### Editable screenshot record
Every saved screenshot record should include:
- Original image
- User-editable title
- Raw OCR text
- AI summary
- Category
- Tags
- Linked task(s)
- Linked note(s)
- Linked event(s)
- Reminder status
- Source date imported
- Screenshot date taken if available
- Confidence score for extraction

### Search and retrieval
- Keyword search over OCR text.[cite:14][cite:21]
- Search by category, tag, date, workspace, and status.[cite:14][cite:18]
- Natural-language search such as “receipt from last month” or “discount code screenshot.” Existing apps explicitly market this behavior.[cite:18]
- Search by extracted entity such as phone number, website, price, subject, or event date.[cite:21]
- Saved search filters.

### Action conversion
- Screenshot to note.
- Screenshot to task.
- Screenshot to event.
- Screenshot to reminder.
- Screenshot to collection/workspace.
- Screenshot to checklist.

Reminder generation from screenshots already exists in the market, while deeper conversion into notes and events would extend the validated behavior into a stronger planning workflow.[cite:14][cite:16]

## Advanced features
### Workspaces and collections
The app should support workspaces such as Personal, Study, Work, Travel, Shopping, and Research. Screenshot apps already use category and collection-based organization, and the productivity layer should extend this into cross-object workspaces that hold notes, tasks, schedules, and screenshots together.[cite:11][cite:12][cite:18]

### Smart review surfaces
- Unprocessed screenshots queue.
- Today to review.
- This week captured.
- Auto-archivable items.
- Duplicate or near-duplicate screenshot suggestions.
- Cleanup suggestions for old screenshots, similar to cleanup features in screenshot organizer apps.[cite:14][cite:16]

### Smart actions
- Tap phone numbers to call, addresses to map, URLs to open, codes to copy, prices to compare. Competing screenshot products already expose click actions on recognized content.[cite:21]
- One-tap “add to calendar” for detected event details.
- One-tap “track expense” for receipt screenshots.
- One-tap “save as study note” for educational screenshots.

### Widgets and quick entry
- Home screen widget for Today list.
- Quick note widget.
- Quick import widget for latest screenshots.
- Timetable widget, reflecting demand already seen in student calendar apps.[cite:20]

### Cross-device and backup
- Cloud backup and sync as a later premium layer.
- Multi-device screenshot and note sync.
- Export to markdown, PDF, or share sheet.
- Local encrypted export.

### Trash and recovery
- Soft-delete with a 30-day recycle bin for tasks, notes, and screenshots, rather than immediate permanent deletion.
- Bulk restore and "empty trash now" control.
- Undo toast on every destructive action (delete, archive, category change) for at least 5 seconds.

## User experience principles
### Capture in under 3 seconds
Creating a task, note, or screenshot record should be near-instant. Screenshot products sell themselves on eliminating endless scrolling, so the UX must feel dramatically faster than using the default gallery.[cite:14][cite:16]

### AI suggests, user decides
AI should prefill title, summary, tags, and actions, but never lock the record. Every important field must be editable so the user maintains trust and control.[cite:15][cite:18] Edits must also stick: if a user retitles a category or corrects a tag, that correction should be remembered for similar future screenshots rather than being overwritten by the next AI pass. A recurring complaint against the closest existing competitor is that AI-generated tags could not be edited at all, which made mis-tagged items permanently hard to find.[27]

### Win the first session
Subscription-app data from 2026 shows that roughly half of paid conversions and the majority of trial cancellations happen on day zero — the first session decides both whether a user pays and whether they come back at all.[31] Concretely, this means: the first screenshot import must produce a visibly useful result (title, category, one action suggestion) before any paywall is shown, and the weekly timetable should be fillable in under two minutes during onboarding so the Today view has real content on first open rather than an empty state.

### Progressive disclosure
The default view should stay clean, with advanced metadata and AI details expandable. The app should not feel like a database front-end for average users.

### Mobile-first structure
The main navigation should be optimized for one-handed use and fast switching among Today, Planner, Capture, Notes, and Search. Since the product is Android-first, the initial information architecture should favor mobile speed over desktop-style complexity.

## Information architecture
Recommended bottom navigation:
- Home
- Planner
- Capture
- Notes
- Search

### Home
- Today agenda
- Priority tasks
- Quick add
- Recent notes
- Unprocessed screenshots

### Planner
- Calendar
- Timetable
- Tasks
- Reminders

### Capture
- Screenshots inbox
- Imports
- Categories
- Smart actions

### Notes
- Recent
- Pinned
- Templates
- Workspace folders

### Search
- Unified search bar
- Filters
- Saved searches
- Smart suggestions

## Detailed feature requirements
### Task requirements
- Manual task creation.
- Task from note/screenshot.
- Due date and reminder support.
- Recurrence rules.
- Priority, tags, and workspace assignment.
- Completion tracking and archive.
- Calendar linking.

### Timetable requirements
- Weekly recurring slots.
- Subject/project metadata.
- Color, room, instructor/client, and notes.
- Conflict detection.
- Timetable export and widget.

### Note requirements
- Inline checklist and formatting tools.
- Linked references to screenshots and tasks.
- Smart headings.
- Template insertion.
- Search inside note content.

### Screenshot requirements
- Detect if item is screenshot versus general image.
- OCR extraction.
- Category prediction.
- Heading and summary generation.
- Tag suggestion.
- Entity extraction.
- Action recommendation.
- Editable review flow.
- Archive, delete, favorite, mark important, and cleanup.[cite:14][cite:21]

## AI system requirements
### AI functions
- OCR text extraction.
- Language detection.
- Classification.
- Summary generation.
- Heading generation.
- Named entity extraction.
- Action suggestion.
- Natural-language search embedding or intent matching.

### Confidence and fallback rules
- If OCR confidence is low, show “Needs review.”
- If summary confidence is low, show extracted bullets instead of a generated narrative.
- If multiple categories score similarly, present top 2 suggestions.
- Always preserve original text and image.

### Privacy model
- On-device OCR preferred.[cite:14][cite:15]
- On-device categorization preferred when feasible.[cite:14][cite:18]
- If cloud summary is used, send text only when possible, following the pattern explicitly described by PixelShot for text-only cloud summarization.[cite:15]
- Provide transparent privacy settings for cloud AI, backup, sync, and retention.

## Functional flows
### Flow 1: Save screenshot to note
1. User takes screenshot.
2. App detects or imports screenshot.
3. OCR extracts text.
4. AI suggests category, title, summary, tags.
5. User edits fields if needed.
6. User taps “Save as note.”
7. App creates note with embedded screenshot, OCR text, summary, and metadata.

### Flow 2: Save screenshot to task
1. Screenshot contains actionable content such as assignment, reminder, or deadline.
2. AI detects date or imperative phrase.
3. App suggests task title, due date, and reminder.
4. User confirms or edits.
5. Task is created and linked back to original screenshot.

### Flow 3: Save screenshot to event
1. Screenshot contains ticket, timetable, meeting, or event details.
2. AI extracts event name, date, time, place.
3. User reviews fields.
4. Event is added to planner and optionally reminders are set.

### Flow 4: Review inbox
1. User opens Capture tab.
2. Inbox shows unprocessed screenshots.
3. User accepts or edits AI suggestions.
4. User archives, converts, or deletes items.
5. Processed items move into searchable library.

## Non-functional requirements
### Performance
- OCR preview results should appear quickly after import.
- Search over OCR text should feel near-instant for local libraries.
- Background processing should not noticeably block app navigation.

### Reliability
- No loss of screenshot metadata during sync or upgrade.
- Offline access to previously processed items.
- Graceful degradation when AI services fail.

### Security and privacy
- Local encryption for sensitive data where feasible.
- Clear consent for screenshot access and notification permissions.
- Fine-grained controls for cloud processing.[cite:14][cite:15]

### Accessibility
- Screen reader friendly labels.
- High-contrast modes.
- Adjustable text size.
- Keyboard support for tablet use.

## Data model
### Core entities
- User
- Workspace
- Task
- Event
- TimetableSlot
- Note
- ScreenshotItem
- Reminder
- Tag
- Attachment
- SearchIndexEntry

### ScreenshotItem schema outline
| Field | Type | Notes |
|---|---|---|
| id | UUID | Primary identifier |
| image_uri | String | Local/cloud reference |
| imported_at | Timestamp | Import time |
| captured_at | Timestamp | Screenshot creation time if available |
| source_type | Enum | Screenshot, imported image, shared image |
| ocr_text | Text | Raw extracted text |
| detected_language | String | Language code |
| ai_title | String | Suggested heading |
| ai_summary | Text | Suggested summary |
| category | Enum | Predicted or confirmed category |
| tags | Array | User and AI tags |
| entities | JSON | Dates, URLs, phones, prices, names |
| confidence | Float | Extraction confidence |
| is_important | Boolean | Important mark |
| status | Enum | New, reviewed, archived, deleted |
| linked_task_ids | Array | Related tasks |
| linked_note_ids | Array | Related notes |
| linked_event_ids | Array | Related events |

## Technical architecture and stack recommendation
The original PRD specifies behavior but not the stack that makes it deliverable on the stated Android-first timeline. The recommendations below are opinionated defaults, not the only valid stack, and should be revisited once a team is in place.

### Platform: native Android first, not a cross-platform framework
This product leans heavily on platform-specific capability — background screenshot detection, `ContentObserver`/`MediaStore` access, notification quick-actions, home-screen widgets, on-device ML — where cross-platform frameworks add an abstraction cost rather than saving time. As of 2026, Flutter remains faster for teams shipping a UI-first app with light native integration, while Kotlin Multiplatform (KMP) is the better fit once an app leans on 4–5+ native system APIs, which this one clearly does.[32][33] The recommendation is therefore:
- **Phase 1 (Android MVP):** native Kotlin + Jetpack Compose. This avoids paying a cross-platform tax on exactly the features (background capture, widgets, notification actions) that differentiate the product.
- **Phase 3 (iOS release):** introduce Kotlin Multiplatform to share the data layer, sync logic, and business rules (task/note/screenshot models, category logic, search indexing) between Android and iOS, while keeping fully native UI on each platform. This avoids a full iOS rewrite without inheriting Flutter's weaker fit for this app's native-API-heavy feature set.

### OCR and on-device AI
- **OCR:** ML Kit Text Recognition v2, on-device, free, no network round-trip required — this satisfies the local-first/privacy pillar directly for the highest-volume operation in the app.[34]
- **On-device summarization/categorization:** ML Kit's GenAI APIs (Summarization, Prompt API), backed by Gemini Nano running in Android's AICore. This is genuinely free and private, but only on supported flagship-tier hardware; Prompt API currently performs best on Pixel 10-class devices and a defined support list, not the median Android device an Indian student is likely to own.[35][36][37]
- **Cloud fallback (required, not optional):** for devices without Gemini Nano support (the majority of the install base in the target segments), route summarization/categorization to a cloud LLM call sending OCR **text only**, never the image, mirroring the privacy pattern already validated by the closest direct competitor.[27] This is also where the existing "text-only cloud AI for summaries" privacy requirement in this PRD becomes concrete rather than aspirational.
- **Cost control:** batch low-priority summarization (e.g., during the nightly review-queue job) rather than calling the cloud model synchronously on every import, and cap free-tier cloud AI calls per day to bound marginal cost per free user — 2026 subscription data shows AI-powered apps carry real per-user inference cost that erodes margins if left unbounded.[31]

### Background capture reliability (a real constraint, not a detail)
Auto-detecting new screenshots depends on background execution, which Android deliberately makes unreliable to save battery: Doze mode defers `ContentObserver` callbacks and `WorkManager` jobs to periodic maintenance windows, and OEM battery managers (common on Xiaomi, Oppo, Vivo — high-share brands in India) add further restrictions on top of stock Android behavior.[38][39] Design implications:
- Treat "auto-detect within N minutes" as a best-effort SLA, not a guarantee, and say so in onboarding rather than over-promising instant capture.
- Use `WorkManager` for the periodic catch-up scan (battery-friendly, survives reboots) plus a `ContentObserver` for the common case where the app is already in memory, rather than relying on either alone.
- Include an explicit "battery optimization" onboarding step that deep-links to the OEM's battery settings for restricted manufacturers, since this is one of the most common root causes of "the app didn't catch my screenshot" support tickets industry-wide.

### Data, search, and sync
- **Local storage:** Room over SQLite, with SQLCipher (or Android's own encrypted storage APIs) for at-rest encryption of screenshot metadata and OCR text, since this is the sensitive-data category the Privacy model section already commits to protecting.
- **Local search:** SQLite FTS5 for keyword/OCR search; add on-device sentence embeddings (a small local embedding model) once natural-language search moves from Post-MVP to active development, keeping semantic search local rather than sending every query to the cloud.
- **Sync/backend:** Firebase (Auth, Firestore or Cloud SQL, Cloud Storage, Cloud Messaging) is a reasonable default for a small team's Phase 3 cloud-sync layer, given fast setup and a generous free tier; a custom backend only pays off once usage or compliance needs (see below) outgrow it.

## Legal, privacy, and platform compliance
This section did not exist in the original draft and is one of the highest-risk gaps, because two of it apply directly to the product's core mechanic (broad, persistent access to a user's screenshots).

### Google Play Photo and Video Permissions policy
Google Play restricts the `READ_MEDIA_IMAGES`/`READ_MEDIA_VIDEO` permissions: apps with only occasional media needs must use the Android Photo Picker (no permission required), while apps that need **persistent, broad** access must pass an access review and file a declaration justifying a qualifying core use case, such as being a genuine photo/video manager.[40][41][42] This product's auto-detect pillar is exactly the kind of "broad, persistent access" the policy is built around, which is good news (a screenshot manager is a textbook qualifying use case) but not automatic — the declaration has to be filed and can be rejected, and a custom picker alone does not exempt the app.[41] Action items to add to Phase 1: file the Play Console declaration early (it gates releases, not just an afterthought), and build the manual-import flow on the Android Photo Picker as a fallback so the app still works for any user who declines broad access.

### India's Digital Personal Data Protection (DPDP) Act, 2023 / DPDP Rules, 2025
Since the primary launch market is India, the DPDP Act applies regardless of company size or revenue — there is no small-developer exemption.[43] Relevant obligations given this product's data (screenshots can contain financial, academic, and identity information):
- Clear, plain-language consent notices before processing personal data, and consent that is not bundled or made a condition of using the app.[44]
- A user-facing mechanism to access, correct, and erase personal data, and to withdraw consent.[45]
- Personal data breach notification obligations to the Data Protection Board and affected users.[45]
- If any part of the student segment is under 18 (a real possibility given school-planner competitors like MyStudyLife and Fhynix target high schoolers, not just college students), the DPDP Act's definition of a child is **under 18**, which is broader than GDPR's, and triggers verifiable parental consent requirements.[46] The simplest way to avoid this compliance burden in an MVP is to explicitly scope the initial launch to college-age users (matching this PRD's primary segment) and defer a high-school-targeted variant until parental-consent flows exist.
- Penalties are substantial (up to ₹250 crore for serious violations) and enforcement has already begun in 2026, so this is not a "deal with it later" item.[47]

### Baseline compliance checklist for MVP
- Published privacy policy and terms of service in plain language, covering what's collected, why, retention period, and third parties (including any cloud AI provider).
- A named grievance/contact channel for data requests, as most small apps need a grievance contact even where a formal Data Protection Officer is not required.[47]
- Encryption in transit (TLS) and at rest for screenshot content and OCR text.
- A data retention and deletion policy (e.g., permanently purge trashed items after 30 days, purge OCR text for deleted screenshots immediately).
- If cloud AI is used, disclose which provider processes text and under what data-retention terms, consistent with the "text-only cloud AI" pattern already assumed elsewhere in this PRD.

## Screens and modules
### Onboarding
- Value proposition.
- Permissions explanation.
- Privacy choices for screenshot access and AI processing.
- Workspace setup.
- Optional timetable setup.

### Main app screens
- Home dashboard.
- Planner calendar/week view.
- Timetable editor.
- Task detail.
- Note editor.
- Screenshot inbox.
- Screenshot detail page.
- Search results.
- Settings and privacy controls.

### Empty states
- No tasks today.
- No timetable created.
- No screenshots imported.
- Search with no results.
- AI unavailable.

## Monetization strategy
A freemium model is likely the best fit because users can understand immediate value from core planning and screenshot organization, while power users will pay for scale, sync, and advanced AI. Some current screenshot apps already distinguish base local functionality from cloud backup or cross-device features.[cite:18]

### Free tier
- Core tasks, notes, timetable.
- Limited screenshot imports per month or per day.
- OCR and basic categorization.
- Manual editing.
- Basic search.

### Premium tier
- Unlimited screenshot processing.
- Advanced AI summaries and semantic search.
- Smart action extraction.
- Cloud backup and sync.[cite:18]
- Multi-device access.
- Advanced templates and exports.
- Workspace sharing.

### Optional monetization add-ons
- Student pack templates.
- Research pack templates.
- Receipt and expense integrations.
- Pro OCR language packs.

### Pricing benchmarks and recommendation
2026 subscription-industry data gives a clearer basis for pricing decisions than intuition alone:
- Freemium converts at roughly 2% by day 35 industry-wide, versus ~10.7% for apps with a hard paywall — but the two models end up at nearly identical one-year retention, so the freemium-vs-paywall choice is really about early cash flow, not long-term quality of user.[48][49]
- Productivity is one of the best-retaining categories overall (around 14% average one-year retention, ahead of most other categories) and leads on median lifetime value across plan types, which supports a subscription model over one-time purchase.[50]
- Annual plans generate roughly 2x the revenue-per-install of monthly plans and ~5x that of weekly plans, so the premium tier's default selection in the paywall should be annual, with monthly as the secondary option.[49]
- Nearly a third of Google Play subscription cancellations are involuntary billing failures (failed cards, expired payment methods) rather than users actively churning — noticeably worse than the App Store's rate.[49][51] For an Android-first product, implementing Play Billing's grace period and account-hold retry logic correctly is a meaningful, low-effort revenue-recovery lever that the original PRD did not mention.

**Recommendation:** keep the freemium structure already defined in this PRD (it fits a discovery-driven, screenshot-habit product better than a hard paywall would), but default new users toward an annual plan at checkout, and treat Play Billing grace-period/retry configuration as an MVP-level engineering task, not a post-launch optimization.

## MVP definition
The MVP should validate that users adopt the combined planner + screenshot workflow, not just one side of it. The first release should therefore include both planning basics and screenshot-to-action conversion rather than shipping them as separate products.[cite:14][cite:15][cite:20]

### MVP features
- Today view.
- Tasks with reminders.
- Weekly timetable.
- Basic notes.
- Screenshot import.
- OCR.
- AI category and title.
- Basic summary.
- Editable screenshot detail.
- Convert screenshot to task or note.
- Keyword search.
- Local-first storage.

### Post-MVP features
- Natural-language search.[cite:18]
- Screenshot to event.
- Duplicate detection and cleanup.[cite:14][cite:16]
- Widgets.[cite:20]
- Calendar sync.
- Cross-device sync.[cite:18]
- Collaboration and sharing.

## Growth, acquisition, and ASO strategy
The original PRD covers positioning and launch tone but not the concrete mechanics of getting the first cohort of users. This section fills that gap.

### App Store Optimization (ASO)
- Primary keyword cluster: screenshot organizer, screenshot to task, class timetable, student planner, AI notes from screenshot. Title and short description should lead with the screenshot-to-action angle, since that's the differentiated half of the product; "planner" alone is the most crowded, least differentiated keyword in this space.
- Store listing screenshots should show the actual screenshot-to-task/note conversion flow in the first two frames, not generic UI shots — this is the feature that has to sell the download.
- Localize the store listing (title, short description, at minimum) for Hindi and Kannada given the initial Bengaluru/VTU-adjacent audience, since regional-language store listings measurably improve conversion in India even when the app UI itself is English-first.

### Growth loops
- **Share-sheet virality:** every exported note, timetable, or converted task carries a small "made with [app]" watermark or footer link when shared to WhatsApp/Slack/email, which is the natural sharing channel for timetables and study notes among student groups.
- **Campus/community seeding:** given the founder's existing standing in campus tech communities (GDG-style developer groups, open-source contributor circles), a low-cost early-adoption path is direct seeding through 2–3 engineering-college communities before a general Play Store launch, using their existing timetable/exam-schedule sharing habits as the initial acquisition hook rather than paid ads.
- **Community launch:** Product Hunt, r/androidapps, r/india, and student-focused subreddits/Discords are the highest-leverage free channels for a screenshot/productivity tool specifically, based on where the competitor apps researched above visibly get their early traction and reviews.
- **Referral mechanic (post-MVP):** unlocking a small premium perk (e.g., one month of unlimited screenshot processing) for both parties when a shared timetable or note results in a new install, which converts the organic "sent my timetable to a friend" behavior into a tracked loop.

### Optional post-MVP extension: regional-language and campus-specific templates
Flagged as an explicit optional extension, not core MVP scope, to stay consistent with the "too broad a feature set" risk already identified. Once the core loop is validated: regional-language OCR (Hindi, Kannada, Tamil) for lecture slides and notice-board screenshots, and pre-built timetable/exam-calendar templates for specific university systems (semester structure, CBCS-style grading) would sharpen the product specifically for the Indian engineering-college segment, where the market research above (Fhynix, MyStudyLife) shows real, active demand but no strong AI-screenshot-native competitor yet.

## Success metrics
### Activation
- Percentage of users who create first task.
- Percentage who import first screenshot.
- Percentage who complete first screenshot-to-task or screenshot-to-note conversion.

### Engagement
- Daily active users.
- Notes created per active user.
- Screenshots processed per active user.
- Percentage of users returning to Today view.
- Weekly review inbox completion rate.

### Retention
- Day 1, Day 7, Day 30 retention.
- Retention difference between screenshot users and non-screenshot users.

### Quality
- OCR success rate.
- AI category acceptance rate.
- AI summary edit rate.
- Search success rate.
- Notification interaction rate.

### Monetization
- Free-to-paid conversion.
- Premium feature attach rate.
- Revenue per active subscriber.

### Instrumentation plan
The metrics above are only collectible if event tracking is designed in from Phase 1, which the original PRD did not specify.
- **North star metric:** weekly count of screenshot-to-action conversions (screenshot → task/note/event) per active user — this is the one number that directly reflects whether the core differentiated loop, not just either half of the app alone, is working.
- **Core event taxonomy:** `screenshot_imported`, `ocr_completed`, `ai_category_suggested`, `ai_category_accepted` / `ai_category_edited`, `screenshot_converted` (with target type), `task_created`, `task_completed`, `timetable_slot_added`, `search_performed` (with result count), `paywall_viewed`, `trial_started`, `subscription_started`.
- **Tooling:** Firebase Analytics (free, integrates naturally with the recommended Firebase backend) for baseline funnels, plus a product-analytics tool such as PostHog or Mixpanel once retention-cohort analysis and feature-flagging needs grow past what Firebase's funnels comfortably support.
- **Privacy note:** analytics events should exclude OCR text, screenshot content, and note bodies — event payloads should be limited to counts, categories, and durations, both to control DPDP-related data exposure and because product analytics doesn't need the content itself to answer "did the loop work."

## Risks and mitigation
### Risk: Too broad a feature set
A combined planner, notes app, and screenshot organizer can become bloated. The mitigation is to keep the MVP focused on one integrated loop: capture, understand, edit, convert, and review.

### Risk: AI quality inconsistency
OCR, summaries, and classification can fail on poor screenshots or mixed layouts. The mitigation is editable outputs, confidence indicators, original text preservation, and user correction loops.[cite:15][cite:18]

### Risk: Privacy concerns
Screenshot data can be highly sensitive. The mitigation is local-first processing, explicit permissions, text-only cloud calls where applicable, and clear settings.[cite:14][cite:15]

### Risk: User confusion about app identity
If the app tries to market every possible use case, users may not understand why it is better than separate tools. The mitigation is a simple initial message centered on planning plus screenshot intelligence.

### Risk: Google Play's media-permissions policy blocks or delays the core feature
Auto-detecting screenshots requires broad, persistent media access, which Google Play now gates behind an access-review declaration rather than granting by default.[41][42] If the declaration is rejected or delayed, the app's headline feature is unusable at launch. The mitigation is to file the Play Console declaration in Phase 1 (not as a launch-week afterthought), and to ship the Android Photo Picker-based manual import as a fully functional fallback so the app is never broken for users, only less automatic, while the review is pending.

### Risk: Background auto-detect is less reliable than the marketing promise
Doze mode, App Standby, and aggressive OEM battery managers (notably on Xiaomi/Oppo/Vivo, common in the target market) can delay or drop the background job that scans for new screenshots.[38][39] Overpromising "instant" detection will generate support tickets and one-star reviews when a screenshot doesn't appear immediately. The mitigation is to set expectations as "processed shortly after capture," provide a manual "scan now" pull-to-refresh as an escape hatch, and guide users through disabling OEM battery restrictions during onboarding.

### Risk: India's DPDP Act compliance gap
Handling screenshots that can contain financial, identity, and academic information makes this a meaningfully regulated data category under India's DPDP Act, with penalties up to ₹250 crore for serious violations and active 2026 enforcement.[43][47] The mitigation is to treat the compliance checklist in Legal, privacy, and platform compliance as an MVP requirement, not a post-launch cleanup task, and to scope the initial launch to college-age users to avoid the additional verifiable-parental-consent burden that applies to under-18 users.[46]

### Risk: Android billing failures quietly erode subscription revenue
Close to a third of Google Play subscription cancellations are involuntary billing failures rather than genuine churn.[49][51] The mitigation is correctly configuring Play Billing's grace period and account-hold/retry settings before charging real subscribers, since this is cheaper to fix at launch than to diagnose later from a revenue dashboard.

## Team, timeline, and execution readiness
The original PRD does not size the work, which makes it hard to sequence realistically. This is a rough planning aid, not a committed estimate.

### Minimum viable team
- One mobile engineer (Kotlin/Compose) covering the planner and screenshot ingestion UI.
- One backend/ML-leaning engineer covering OCR pipeline orchestration, cloud AI fallback, and sync backend.
- Part-time or fractional product design support for the onboarding flow and screenshot-review UI specifically, since these two flows carry most of the day-zero conversion risk described above.
- A single person can credibly build the Phase 1 MVP solo if full-stack across Kotlin, backend, and basic ML integration, but the review-queue UI, OCR pipeline, and compliance checklist together are enough work that a second contributor materially de-risks the timeline.

### Rough phase-level timeline
- Phase 1 (Android MVP, as scoped below): 10–14 weeks for a 1–2 person team, assuming native Kotlin and the ML Kit/Gemini Nano-plus-cloud-fallback stack recommended above rather than building OCR or an LLM pipeline from scratch.
- Phase 2 (retention and intelligence): 6–8 weeks, mostly serial to Phase 1 since semantic search and duplicate detection depend on Phase 1's data model being stable.
- Phase 3 (monetization and scale, including iOS): 10–16 weeks, materially longer if the iOS release is treated as a true parallel platform rather than sequenced after Android KMP groundwork is in place.

### QA and device coverage
Screenshot OCR and background-capture behavior are unusually device-sensitive compared to typical CRUD app features. Before general release, test explicitly on: at least one Xiaomi/Oppo/Vivo device (aggressive battery managers), one budget device with 4GB RAM or less (common in the student segment), and one Gemini Nano-capable flagship (to validate the on-device AI path, not just the cloud fallback). Add Firebase Crashlytics (or an equivalent) from the first internal build, not after the first crash report arrives from a real user.

## Roadmap
### Phase 1: Android MVP
- Today view
- Tasks and reminders
- Timetable
- Notes
- Screenshot import
- OCR, category, title, summary
- Search
- Screenshot to task/note
- Google Play media-permissions declaration filed, with Photo Picker fallback shipped
- Privacy policy, terms of service, and DPDP-compliant consent flow published
- Analytics event taxonomy and crash reporting wired up from the first internal build

### Phase 2: Retention and intelligence
- Semantic search
- Screenshot to event
- Duplicate cleanup
- Better widgets
- Smart review queue
- Improved extraction models

### Phase 3: Monetization and scale
- Premium subscriptions
- Cloud backup and sync
- Shared workspaces
- Export and integrations
- iOS release

## Launch strategy
The launch should emphasize a narrow, vivid problem statement rather than general productivity. App store positioning should focus on turning screenshots into useful notes, reminders, and plans, because current market products already show discoverability around screenshot organization, OCR, summaries, and reminder generation.[cite:14][cite:15][cite:21]

A practical launch sequence is Android-first with student and heavy screenshot users as the initial audience. School planner demand remains visible through timetable- and assignment-centric apps, while screenshot organizer apps show a newer but growing category that can differentiate the product in stores.[cite:17][cite:20][cite:22]

## Final recommendation
The strongest version of this product is not a generic all-in-one workspace. It is a mobile-first planning system built around a differentiated screenshot intelligence engine that helps users capture information, understand it quickly, and convert it into structured action.[cite:14][cite:15][cite:18]

That combination is commercially promising because the planning side is proven, the screenshot side is emerging, and the bridge between them remains underdeveloped in the current market. A disciplined MVP focused on tasks, timetable, notes, OCR, categorization, editable summaries, and screenshot conversion offers the clearest path to product-market validation.[cite:14][cite:15][cite:20]

What this revision changes: the original draft was strong on product behavior and weak on four things that decide whether the product actually ships and survives contact with the Play Store — a named competitive set, the technical stack that makes the AI/OCR pipeline real, the Google Play and India DPDP compliance work the core feature depends on, and the growth/pricing mechanics that turn a working app into an installed one. Those are now covered above as Competitive landscape, Technical architecture and stack recommendation, Legal, privacy, and platform compliance, Growth, acquisition, and ASO strategy, and Team, timeline, and execution readiness, with the Monetization and Success metrics sections extended in place.

## Sources referenced in this revision
Bracketed numbers like [23]–[51] mark claims added in this revision and are sourced below. Bracketed markers like [cite:14] belong to the original draft and reference research that was not included in the uploaded file, so they could not be re-verified here.

1. Sorti — [23] https://letitsorti.com/journal/best-app-to-organize-screenshots-iphone-android
2. MarkIt / Fabric — [24] https://mark-it.co/screenshot-organizer-app · https://fabric.so/screenshot-ai
3. Student planner roundup (myHomework) — [25] https://blog.planwiz.app/top-daily-planner-apps-for-students/
4. MyStudyLife (official) — [26] https://mystudylife.com/
5. PixelShot (Google Play listing) — [27] https://play.google.com/store/apps/details?id=aculix.pixelshot.app&hl=en_US
6. Fhynix — [28] https://fhynix.com/best-school-planner-apps/
7. Revu student planner comparison (Notion, Todoist, Akiflow, etc.) — [29] https://revu.co.in/tools/best-study-planner-apps
8. RevenueCat, State of Subscription Apps 2026 (Productivity) — [31] https://www.revenuecat.com/state-of-subscription-apps-2026-productivity/
9. Volpis, Kotlin Multiplatform vs Flutter 2026 — [32] https://volpis.com/blog/kotlin-multiplatform-vs-flutter/
10. Java Code Geeks, KMP vs Flutter vs React Native 2026 — [33] https://www.javacodegeeks.com/2026/02/kotlin-multiplatform-vs-flutter-vs-react-native-the-2026-cross-platform-reality.html
11. Atomic Robot, On-device OCR with ML Kit Text Recognition v2 — [34] https://atomicrobot.com/blog/mlkit-on-device-ocr-android/
12. Android Developers, Gemini Nano — [35] https://developer.android.com/ai/gemini-nano
13. Local AI Master, Gemini Nano Android Guide — [36] https://localaimaster.com/blog/gemini-nano-android-guide
14. Android Developers Blog, ML Kit's Prompt API (device support) — [37] https://developer.android.com/blog/posts/ml-kit-s-prompt-api-unlock-custom-on-device-gemini-nano-experiences
15. ProAndroidDev, Beyond Doze — [38] https://proandroiddev.com/beyond-doze-building-reliable-background-execution-on-modern-android-including-oem-realities-5fa0a6e05672
16. softAai, Building Resilient Android Apps — [39] https://softaai.com/building-resilient-android-apps-surviving-doze-standby/
17. Google Play Console Help, Photo and Video Permissions policy — [40] https://support.google.com/googleplay/android-developer/answer/14115180?hl=en
18. Google Play Console Help, Restricted Permissions minimum scope — [41] https://support.google.com/googleplay/android-developer/answer/16935362?hl=en
19. Android Developers Blog, Prioritize media privacy with Photo Picker — [42] https://android-developers.googleblog.com/2025/04/google-play-empowering-developers-to-build-user-trust-through-privacy.html
20. Respectlytics, India DPDP Act for Mobile Apps — [43] https://respectlytics.com/blog/india-dpdp-act-mobile-app-compliance/
21. Innovatrix Infotech, DPDP Act 2026 compliance guide — [44] https://www.innovatrixinfotech.com/blog/data-privacy-compliance-india-dpdp-act-2026
22. EY India, Transforming data privacy: DPDP Act 2023 and DPDP Rules 2025 — [45] https://www.ey.com/en_in/insights/cybersecurity/transforming-data-privacy-digital-personal-data-protection-rules-2025
23. Atlas Systems, Digital Personal Data Protection Act India compliance guide — [46] https://www.atlassystems.com/blog/digital-personal-data-protection-act-india
24. RecordingLaw, India Data Privacy Laws — [47] https://www.recordinglaw.com/world-laws/world-data-privacy-laws/india-data-privacy-laws/
25. FunnelFox, App Pricing Models for 2026 — [48] https://blog.funnelfox.com/app-pricing-models-guide/
26. RevenueCat, State of Subscription Apps — [49] https://www.revenuecat.com/state-of-subscription-apps/
27. Adapty, State of In-App Subscriptions Report 2026 — [50] https://adapty.io/state-of-in-app-subscriptions-report/
28. RevenueCat, State of Subscription Apps in 10 minutes (2026) — [51] https://www.revenuecat.com/blog/growth/subscription-app-trends-benchmarks-2026/

All sources were current as of July 2026 and should be re-checked before final decisions, since policy pages (Google Play, DPDP Rules) in particular are updated on their own schedules.
