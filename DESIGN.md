---
name: CaptureFlow
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#3b494a'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#6b7a7a'
  outline-variant: '#bac9c9'
  surface-tint: '#00696c'
  primary: '#00696c'
  on-primary: '#ffffff'
  primary-container: '#37e1e6'
  on-primary-container: '#006063'
  inverse-primary: '#2cdbe0'
  secondary: '#006b5f'
  on-secondary: '#ffffff'
  secondary-container: '#6df5e1'
  on-secondary-container: '#006f64'
  tertiary: '#006c49'
  on-tertiary: '#ffffff'
  tertiary-container: '#55e4a8'
  on-tertiary-container: '#006343'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#59f8fd'
  primary-fixed-dim: '#2cdbe0'
  on-primary-fixed: '#002021'
  on-primary-fixed-variant: '#004f51'
  secondary-fixed: '#71f8e4'
  secondary-fixed-dim: '#4fdbc8'
  on-secondary-fixed: '#00201c'
  on-secondary-fixed-variant: '#005048'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-caps:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  3xl: 64px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style
The design system is rooted in high-performance minimalism, prioritizing clarity, focus, and intentionality. It is designed for power users who value the aesthetic precision of tools like Linear and Things 3. The brand personality is professional yet approachable, utilizing vast whitespace to reduce cognitive load and create a "breathable" productivity environment.

The style is **Corporate / Modern** with a focus on structural purity. It avoids decorative flourishes like glassmorphism or vibrant gradients in favor of stable, flat surfaces and precise geometry. The emotional response should be one of calm control and systematic efficiency.

## Colors
This design system utilizes a restrained palette to maintain focus on user content. The **Primary** color is a bright, energetic Cyan (`#37e1e6`), used for core actions and active states to provide a high-tech, modern feel. The **AI Accent** (Teal) is reserved strictly for intelligent features or automated suggestions to distinguish them from standard manual tasks.

The background is a cool, very light gray (`#f8fafc`), which provides enough contrast for white surface cards to appear distinct without needing heavy borders. Text utilizes a high-contrast charcoal for primary information and a soft slate for metadata and secondary labels.

## Typography
The system relies exclusively on **Inter** to achieve a systematic, utilitarian aesthetic. Typography is used as a structural element rather than decoration. 

Key hierarchy rules:
- **Tight Letter Spacing:** Headlines use a slightly negative letter spacing (`-0.01em` to `-0.02em`) to appear more "locked-in" and editorial.
- **Labeling:** Small labels (`label-caps`) are used for category headers or metadata, providing clear sectioning without adding visual weight.
- **Readability:** Body text maintains a generous line height to ensure long-form notes and task lists remain legible during deep work.

## Layout & Spacing
The design system follows a strict **8pt spacing system**. All dimensions, padding, and margins must be multiples of 8 (or 4 for fine-tuning small components).

- **Layout Model:** Use a 12-column fluid grid for desktop and a 4-column grid for mobile.
- **Margins:** Standard mobile screens use a 16px side margin. Desktop layouts should use a max-width container (1200px) centered in the viewport.
- **Vertical Rhythm:** Content blocks should typically be separated by `2xl` (48px) to maintain the signature "large whitespace" aesthetic.

## Elevation & Depth
This design system uses **Tonal Layers** combined with **Ambient Shadows** to create a sense of hierarchy.

- **Level 0 (Background):** `#f8fafc` — The canvas.
- **Level 1 (Cards/Surfaces):** `#FFFFFF` — Used for the primary content blocks. These elements feature a very soft, diffused shadow: `0px 4px 12px rgba(0, 0, 0, 0.05)`.
- **Level 2 (Modals/Popovers):** Elevated `#FFFFFF` surfaces with a more pronounced shadow: `0px 12px 32px rgba(0, 0, 0, 0.1)`.

Avoid using borders for elevation. Depth should be felt through subtle shadow shifts and the contrast between the background and surface colors.

## Shapes
The shape language is defined by **Rounded** corners that soften the technical precision of the layout. 

- **Primary Cards:** Always use 16px (`rounded-lg`) corner radii to create a friendly, modern container.
- **Small Components:** Buttons and input fields use 8px (`rounded-md`) to maintain a tighter, more functional appearance.
- **Icons:** Should feature slightly rounded terminals and corners to match the UI's geometry.

## Components
- **Buttons:** Primary buttons are solid Cyan (`#37e1e6`) with high-contrast text. Secondary buttons are subtle: a light gray background (`#F1F5F9`) with primary text. No heavy borders.
- **Cards:** The core of the system. Cards have no borders, 16px rounding, and the "Level 1" ambient shadow. Padding inside cards defaults to 24px (`lg`).
- **Inputs:** Clean, 8px rounded fields with a 1px border in `#E2E8F0`. Focus states should transition the border to the primary Cyan with a 2px thickness.
- **Chips/Tags:** Small, 4px rounded capsules with low-contrast backgrounds (e.g., light teal background for AI tags with dark teal text).
- **Task Lists:** Use a clean list layout with 16px vertical spacing between items. Checkboxes are custom-styled circles that transition from an outline to a solid Cyan fill upon completion.
- **Navigation:** A minimal sidebar on desktop and a clean bottom-bar on mobile, using 24px stroke-based icons and `label-md` text.