(ns utils.worklets.shell)

(def shell-worklets (js/require "../src/js/worklets/shell.js"))

(defn stack-opacity
  [id selected-stack-id]
  (.stackOpacity ^js shell-worklets id selected-stack-id))

(defn stack-z-index
  [id selected-stack-id]
  (.stackZIndex ^js shell-worklets id selected-stack-id))

(defn bottom-tabs-height
  [home-stack-state-sv container-height extended-container-height]
  (.bottomTabsHeight ^js shell-worklets home-stack-state-sv container-height extended-container-height))

(defn bottom-tab-icon-color
  [id selected-stack-id-sv home-stack-state-sv pass-through-sv selected-tab-color default-color
   pass-through-color]
  (.bottomTabIconColor ^js shell-worklets
                       id
                       selected-stack-id-sv
                       home-stack-state-sv
                       pass-through-sv
                       selected-tab-color
                       default-color
                       pass-through-color))

(defn home-stack-opacity
  [home-stack-state-sv]
  (.homeStackOpacity ^js shell-worklets home-stack-state-sv))

(defn home-stack-pointer
  [home-stack-state-sv]
  (.homeStackPointer ^js shell-worklets home-stack-state-sv))

(defn home-stack-scale
  [home-stack-state-sv scale]
  (.homeStackScale ^js shell-worklets home-stack-state-sv scale))

(defn home-stack-left
  [selected-stack-id-sv animate-home-stack-left home-stack-state-sv left-home-stack-position]
  (.homeStackLeft
   ^js shell-worklets
   selected-stack-id-sv
   animate-home-stack-left
   home-stack-state-sv
   left-home-stack-position))

(defn home-stack-top
  [home-stack-state-sv top-home-stack-position]
  (.homeStackTop ^js shell-worklets home-stack-state-sv top-home-stack-position))
