(ns legacy.status-im.ui.components.colors
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]))

;; Colors mapping from figma to code, note that theme is more extended and
;; one can follow the comments from the light theme to choose what to use in a component.
(comment
  {"Accent blue, #4360DF"               [:interactive-01 :text-04 :icon-04]
   "Accent blue as background, #ECEFFC" [:interactive-02]
   "Dark grey, #939BA1"                 [:text-02 :icon-02]
   "Black"                              [:text-01 :icon-01]
   "Main Green/Success, #4EBC60"        [:positive-01]
   "Shades 10% green, #EDFBEF"          [:positive-02]
   "Main Red/Error, #FF2D55"            [:negative-01]
   "Shades 10% Red, #FFEAEE"            [:negative-02]
   "Light grey, #EEF2F5"                [:ui-01]
   "White, #FFFFFF"                     [:ui-background]
   "Devider, 0.1 of black"              [:ui-02]})

(def light-theme
  {:positive-01    "rgba(68,208,88,1)"     ; Primary Positive, text, icons color
   :positive-02    "rgba(78,188,96,0.1)"   ; Secondary Positive, Supporting color for success
   ; illustrations
   :positive-03    "rgba(78,188,96,1)"     ; Lighter Positive, Supporting color for success illustrations
   :negative-01    "rgba(255,45,85,1)"     ; Primary Negative, text, icons color
   :negative-02    "rgba(255,45,85,0.1))"  ; Secondary Negative, Supporting color for errors
   ; illustrations
   :warning-01     "rgba(255, 202, 15, 1)"
   :warning-02     "rgba(255, 202, 15, 0.1)"
   :interactive-01 "rgba(67,96,223,1)"     ; Accent color, buttons, own message, actions,active state
   :interactive-02 "rgba(236,239,252,1)"   ; Light Accent, buttons background, actions background,
   ; messages
   :interactive-03 "rgba(255,255,255,0.1)" ; Background for interactive above accent
   :interactive-04 "rgba(147,155,161,1)"   ; Disabled state
   :ui-background  "rgba(255,255,255,1)"   ; Default view background
   :ui-01          "rgba(238,242,245,1)"   ; Secondary background
   :ui-02          "rgba(0,0,0,0.1)"       ; Deviders
   :ui-03          "rgba(0,0,0,0.86)"      ; Dark background
   :text-01        "rgba(0,0,0,1)"         ; Main text color
   :text-02        "rgba(147,155,161,1)"   ; Secondary text
   :text-03        "rgba(255,255,255,0.7)" ; Secondary on accent
   :text-04        "rgba(67,96,223,1)"     ; Links text color
   :text-05        "rgba(255,255,255,1)"   ; Text inverse on accent
   :icon-01        "rgba(0,0,0,1)"         ; Primary icons
   :icon-02        "rgba(147,155,161,1)"   ; Secondary icons
   :icon-03        "rgba(255,255,255,0.4)" ; Secondary icons on accent bg
   :icon-04        "rgba(67,96,223,1)"     ; Interactive icon
   :icon-05        "rgba(255,255,255,1)"   ; Icons inverse on accent background
   :shadow-01      "rgba(0,9,26,0.12)"     ; Main shadow color
   :backdrop       "rgba(0,0,0,0.4)"       ; Backdrop for modals and bottom sheet
   :border-01      "rgba(238,242,245,1)"
   :border-02      "rgba(67, 96, 223, 0.1)"
   :highlight      "rgba(67,96,223,0.4)"
   :blurred-bg     "rgba(255,255,255,0.3)"})

(def dark-theme
  {:positive-01    "rgba(68,208,88,1)"
   :positive-02    "rgba(78,188,96,0.1)"
   :positive-03    "rgba(78,188,96,1)"
   :negative-01    "rgba(252,95,95,1)"
   :negative-02    "rgba(252,95,95,0.1)"
   :warning-01     "rgba(255, 202, 15, 1)"
   :warning-02     "rgba(255, 202, 15, 0.1)"
   :interactive-01 "rgba(97,119,229,1)"
   :interactive-02 "rgba(35,37,47,1)"
   :interactive-03 "rgba(255,255,255,0.1)"
   :interactive-04 "rgba(131,140,145,1)"
   :ui-background  "rgba(20,20,20,1)"
   :ui-01          "rgba(37,37,40,1)"
   :ui-02          "rgba(0,0,0,0.1)"
   :ui-03          "rgba(0,0,0,0.86)"
   :text-01        "rgba(255,255,255,1)"
   :text-02        "rgba(131,140,145,1)"
   :text-03        "rgba(255,255,255,0.7)"
   :text-04        "rgba(97,119,229,1)"
   :text-05        "rgba(20,20,20,1)"
   :icon-01        "rgba(255,255,255,1)"
   :icon-02        "rgba(131,140,145,1)"
   :icon-03        "rgba(255,255,255,0.4)"
   :icon-04        "rgba(97,119,229,1)"
   :icon-05        "rgba(20,20,20,1)"
   :shadow-01      "rgba(0,0,0,0.75)"
   :backdrop       "rgba(0,0,0,0.4)"
   :border-01      "rgba(37,37,40,1)"
   :border-02      "rgba(97,119,229,0.1)"
   :highlight      "rgba(67,96,223,0.4)"
   :blurred-bg     "rgba(0,0,0,0.3)"})

(def theme (reagent/atom light-theme))

(defn get-color
  [color]
  (get @theme color))

;; LEGACY COLORS

(defn alpha
  [value opacity]
  (if (string/starts-with? value "#")
    (let [hex (string/replace value #"#" "")
          r   (js/parseInt (subs hex 0 2) 16)
          g   (js/parseInt (subs hex 2 4) 16)
          b   (js/parseInt (subs hex 4 6) 16)]
      (str "rgba(" r "," g "," b "," opacity ")"))
    (let [rgb (string/split value #",")]
      (str (string/join "," (butlast rgb)) "," opacity ")"))))

(def old-colors-mapping-light
  {:mentioned-background "#def6fc"
   :mentioned-border     "#b8ecf9"
   :pin-background       "#FFEECC"})

(def old-colors-mapping-dark
  {:mentioned-background "#2a4046"
   :mentioned-border     "#2a4046"
   :pin-background       "#34232B"})

(def old-colors-mapping-themes {:dark old-colors-mapping-dark :light old-colors-mapping-light})

;; WHITE
(def white (:ui-background light-theme))
(def white-persist (:ui-background light-theme))                          ;; this doesn't with theme
(def white-transparent-10 (:interactive-03 light-theme))                ;; Used as icon background color for a dark foreground
(def white-transparent (:icon-03 light-theme))                   ;; Used as icon color on dark background and input placeholder color
(def white-transparent-persist (:icon-03 light-theme))
(def white-transparent-70 (:text-03 light-theme))
(def white-transparent-70-persist (:text-03 light-theme))

(def mentioned-background (:mentioned-background old-colors-mapping-light))
(def mentioned-border (:mentioned-border old-colors-mapping-light))

(def red-light "#ffe5ea")                                   ;; error tooltip TODO (andrey) should be white, but shadow needed

;; BLACK
(def black (:text-01 light-theme))                                  ;; Used as the default text color
(def black-persist (:ui-background dark-theme))                           ;; this doesn't with theme
(def black-transparent (:ui-02 light-theme))                   ;; Used as background color for rounded button on dark background and as background
;; color for containers like "Backup recovery phrase"
(def black-transparent-20 (:backdrop light-theme))                ; accounts divider
(def black-transparent-40 (:backdrop light-theme))
(def black-transparent-40-persist (:backdrop light-theme))
(def black-transparent-50 (:backdrop light-theme))
(def black-light "#2d2d2d")                                 ;; sign-with-keycard-button
(def black-transparent-86 (:ui-03 light-theme))

;; DARK GREY
(def gray (:text-02 light-theme))                                    ;; Dark grey, used as a background for a light foreground and as
;; section header and secondary text color
(def gray-transparent-10 (alpha gray 0.1))
(def gray-transparent-40 (alpha gray 0.4))
;; LIGHT GREY
(def gray-lighter (:ui-01 light-theme))                    ;; Light Grey, used as a background or shadow

;; ACCENT BLUE
(def blue (:interactive-01 light-theme))                                    ;; Accent blue, used as main wallet color, and ios home add button
(def blue-persist (:interactive-01 light-theme))
;; LIGHT BLUE
(def blue-light (:interactive-02 light-theme))                        ;; Light Blue
(def blue-transparent-10 (alpha blue 0.1))                  ;; unknown

;; RED
(def red (:negative-01 light-theme))                                      ;; Used to highlight errors or "dangerous" actions
(def red-transparent-10 (alpha red 0.1))                    ;;action-row ;; ttt finish
(def red-audio-recorder "#fa6565")

;; GREEN
(def green "#44d058")                                       ;; icon for successful inboud transaction
(def green-transparent-10 (alpha green 0.1))                ;; icon for successful inboud transaction

;; YELLOW
(def pin-background (:pin-background old-colors-mapping-light))                    ;; Light yellow, used as background for pinned messages

(def purple "#887af9")
(def orange "#FE8F59")

(def chat-colors
  ["#fa6565"
   "#7cda00"
   purple
   "#51d0f0"
   orange
   "#d37ef4"])

(def account-colors
  ["#9B832F"
   "#D37EF4"
   "#1D806F"
   "#FA6565"
   "#7CDA00"
   purple
   "#8B3131"])

(def mention-incoming "#0DA4C9")
(def mention-outgoing "#9EE8FA")
(def text black)
(def text-gray gray)
(def default-community-color "#773377")

(def default-chat-color "#a187d5")                          ;; legacy

;; THEME

(def theme-type (reagent/atom :light))

(defn dark?
  []
  (= :dark @theme-type))

(defn set-legacy-theme-type
  [type]
  (when-not (= type @theme-type)
    (let [old-colors-mapping-colors (get old-colors-mapping-themes type)]
      (set! white (:ui-background @theme))
      (set! black (:text-01 @theme))
      (set! gray-lighter (:ui-01 @theme))
      (set! blue (:interactive-01 @theme))
      (set! gray (:text-02 @theme))
      (set! blue-light (:interactive-02 @theme))
      (set! red (:negative-01 @theme))
      (set! text black)
      (set! mentioned-background (:mentioned-background old-colors-mapping-colors))
      (set! mentioned-border (:mentioned-border old-colors-mapping-colors))
      (set! white-transparent-10 (alpha white 0.1))
      (set! white-transparent (alpha white 0.4))
      (set! white-transparent-70 (alpha white 0.7))
      (set! black-transparent (alpha black 0.1))
      (set! black-transparent-20 (alpha black 0.2))
      (set! black-transparent-40 (alpha black 0.4))
      (set! black-transparent-50 (alpha black 0.5))
      (set! gray-transparent-10 (alpha gray 0.1))
      (set! gray-transparent-40 (alpha gray 0.4))
      (set! green-transparent-10 (alpha green 0.1))
      (set! red-transparent-10 (alpha red 0.1))
      (set! blue-transparent-10 (alpha blue 0.1))
      (set! pin-background (:pin-background old-colors-mapping-colors)))
    (reset! theme-type type)))

;; Colors related to Visibility Status
(def color-online "#7CDA00")
(def color-dnd "#FA6565")
(def color-inactive "#939BA1")
