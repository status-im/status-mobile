(ns utils.worklets.shell)

(def bottom-tabs-worklets (js/require "../src/js/worklets/shell/bottom_tabs.js"))
(def home-stack-worklets (js/require "../src/js/worklets/shell/home_stack.js"))
(def floating-screen-worklets (js/require "../src/js/worklets/shell/floating_screen.js"))

;; Derived values for Bottom tabs
(defn bottom-tabs-height
  [home-stack-state-sv container-height extended-container-height]
  (.bottomTabsHeight ^js bottom-tabs-worklets
                     home-stack-state-sv
                     container-height
                     extended-container-height))

(defn bottom-tab-icon-color
  [id selected-stack-id-sv home-stack-state-sv pass-through-sv selected-tab-color default-color
   pass-through-color]
  (.bottomTabIconColor ^js bottom-tabs-worklets
                       id
                       selected-stack-id-sv
                       home-stack-state-sv
                       pass-through-sv
                       selected-tab-color
                       default-color
                       pass-through-color))

;; Derived values for stacks (communities, chat, wallet, browser)
(defn stack-opacity
  [id selected-stack-id]
  (.stackOpacity ^js home-stack-worklets id selected-stack-id))

(defn stack-z-index
  [id selected-stack-id]
  (.stackZIndex ^js home-stack-worklets id selected-stack-id))

;; Derived values for Home stack (container)
(defn home-stack-opacity
  [home-stack-state-sv]
  (.homeStackOpacity ^js home-stack-worklets home-stack-state-sv))

(defn home-stack-pointer
  [home-stack-state-sv]
  (.homeStackPointer ^js home-stack-worklets home-stack-state-sv))

(defn home-stack-scale
  [home-stack-state-sv scale]
  (.homeStackScale ^js home-stack-worklets home-stack-state-sv scale))

(defn home-stack-left
  [selected-stack-id-sv animate-home-stack-left home-stack-state-sv left-home-stack-position]
  (.homeStackLeft
   ^js home-stack-worklets
   selected-stack-id-sv
   animate-home-stack-left
   home-stack-state-sv
   left-home-stack-position))

(defn home-stack-top
  [home-stack-state-sv top-home-stack-position]
  (.homeStackTop ^js home-stack-worklets home-stack-state-sv top-home-stack-position))

(defn home-stack-border-radius
  [home-stack-state-sv]
  (.homeStackBorderRadius ^js home-stack-worklets home-stack-state-sv))

;; Derived values for floating screen
(defn floating-screen-left
  [screen-state screen-width switcher-card-left-position]
  (.screenLeft ^js floating-screen-worklets screen-state screen-width switcher-card-left-position))

(defn floating-screen-top
  [screen-state switcher-card-top-position]
  (.screenTop ^js floating-screen-worklets screen-state switcher-card-top-position))

(defn floating-screen-width
  [screen-state screen-width switcher-card-size]
  (.screenWidth ^js floating-screen-worklets screen-state screen-width switcher-card-size))

(defn floating-screen-height
  [screen-state screen-height switcher-card-size]
  (.screenHeight ^js floating-screen-worklets screen-state screen-height switcher-card-size))

(defn floating-screen-z-index
  [screen-state]
  (.screenZIndex ^js floating-screen-worklets screen-state))
