(ns status-im2.contexts.chat.messages.constants)

;;;; Navigation
(def ^:const top-bar-height 56)
(def ^:const pinned-banner-height 40)
(def ^:const header-container-top-margin 48)
(def ^:const header-container-radius 20)
(def ^:const header-animation-distance 20)
(def ^:const content-animation-start-position 110)
;; Note - We should also consider height of bio for banner animation starting position
;; Todo - Should be updated once New-profile implemation is complete
(def ^:const pinned-banner-animation-start-position 148)

(def ^:const default-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})
