(ns status-im.ui.components.button.haptic
  (:require [oops.core :refer [ocall]]
            [react-native-haptic-feedback :default react-native-haptic-feedback]))

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
  (ocall react-native-haptic-feedback "trigger" (get haptic-methods method)))
