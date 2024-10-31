(ns status-im.contexts.keycard.nfc.effects
  (:require [keycard.keycard :as keycard]
            [utils.re-frame :as rf]))

(rf/reg-fx :effects.keycard/check-nfc-enabled
 (fn []
   (keycard/check-nfc-enabled
    {:on-success #(rf/dispatch [:keycard/on-check-nfc-enabled-success %])})))

(rf/reg-fx :effects.keycard.ios/start-nfc keycard/start-nfc)
(rf/reg-fx :effects.keycard.ios/stop-nfc keycard/stop-nfc)
