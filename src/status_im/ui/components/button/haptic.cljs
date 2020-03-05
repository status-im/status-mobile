(ns status-im.ui.components.button.haptic
  (:require [oops.core :refer [oget ocall]]
            [status-im.react-native.js-dependencies :as js-deps]))

(def haptic-feedback (oget js-deps/react-native-haptic-feedback "default"))

(def haptic-methods
  {:selection            "selection"
   :impacet-light        "impactLight"
   :impact-medium        "impactMedium"
   :impact-heavy         "impactHeavy"
   :notification-success "notificationSuccess"
   :notification-warning "notificationWarning"
   :notification-error   "notificationError"
   :clock-tick           "clockTick"                    ; (Android only)
   :context-click        "contextClick"                 ; (Android only)
   :keyboard-press       "keyboardPress"                ; (Android only)
   :keyboard-release     "keyboardRelease"              ; (Android only)
   :keyboard-tap         "keyboardTap"                  ; (Android only)
   :long-press           "longPress"                    ; (Android only)
   :text-handle-move     "textHandleMove"               ; (Android only)
   :virtual-key          "virtualKey"                   ; (Android only)
   :virtual-key-release  "virtualKeyRelease"            ; (Android only)
})

(defn trigger [method]
  (ocall haptic-feedback "trigger" (get haptic-methods method)))
