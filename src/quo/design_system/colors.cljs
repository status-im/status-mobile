(ns quo.design-system.colors
  (:require [reagent.core :as reagent]))

(def white "#FFFFFF")
(def black "#000000")

;; Colors mapping from figma to code, note that theme is more extended and
;; one can follow the comments from the light theme to choose what to use in a component.
(comment
  {"Accent blue, #4360DF"               [:interactive-01 :text-04]
   "Accent blue as background, #ECEFFC" [:interactive-02]
   "Dark grey, #939BA1"                 [:text-02 :icon-02]
   "Black"                              [:text-01 :icon-01]
   "Main Green/Success, #4EBC60"        [:positive-01]
   "Shades 10% green, #EDFBEF"          [:positive-02]
   "Main Red/Error, #FF2D55"            [:negative-01]
   "Shades 10% Red, #FFEAEE"            [:negative-02]
   "Light grey, #EEF2F5"                [:ui-01]
   "White, #FFFFFF"                     [:ui-background :icon-04]
   "Devider, 0.1 of black"              [:ui-02]})

(def light-theme
  {:positive-01    "rgba(68,208,88,1)"     ; Primary Positive, text, icons color
   :positive-02    "rgba(78,188,96,0.1)"   ; Secondary Positive, Supporting color for success illustrations
   :negative-01    "rgba(255,45,85,1)"     ; Primary Negative, text, icons color
   :negative-02    "rgba(255,45,85,0.1))"  ; Secondary Negative, Supporting color for errors illustrations
   :interactive-01 "rgba(67,96,223,1)"     ; Accent color, buttons, own message, actions,active state
   :interactive-02 "rgba(236,239,252,1)"   ; Light Accent, buttons background, actions background, messages
   :interactive-03 "rgba(255,255,255,0.1)" ; Background for interactive above accent
   :ui-background  "rgba(255,255,255,1)"   ; Default view background
   :ui-01          "rgba(238,242,245,1)"   ; Secondary background
   :ui-02          "rgba(0,0,0,0.1)"       ; Deviders
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
   })

(def dark-theme
  {:positive-01    "rgba(68,208,88,1)"
   :positive-02    "rgba(78,188,96,0.1)"
   :negative-01    "rgba(252,95,95,1)"
   :negative-02    "rgba(252,95,95,0.1)"
   :interactive-01 "rgba(97,119,229,1)"
   :interactive-02 "rgba(35,37,47,1)"
   :interactive-03 "rgba(255,255,255,0.1)"
   :ui-background  "rgba(20,20,20,1)"
   :ui-01          "rgba(37,37,40,1)"
   :ui-02          "rgba(0,0,0,0.1)"
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
   :backdrop       "rgba(0,0,0,0.4)"})

(def theme (reagent/atom light-theme))
