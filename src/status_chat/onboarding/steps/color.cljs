(ns status-chat.onboarding.steps.color
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-chat.components.chat.composer :as chat.composer]
            [status-chat.components.chat.message :as chat.message]
            [status-chat.onboarding.constants :as constants]
            [utils.re-frame :as rf]))

(defn message
  [{:keys [reply]}]
  [:<>
   [chat.message/text "Small talk time ... so what's your favorite color?"]
   (when reply
     [chat.message/extra-content {:on-press identity}
      [chat.message/text (:input-value reply)]])])

(defn change-color
  [value]
  (rf/dispatch [:onboarding/store-reply :color {:input-value value}]))

(defn submit-color
  [value]
  (rf/dispatch [:onboarding/submit-reply :color {:input-value value}]))

(defn composer
  []
  (let [[color-value set-color-value] (rn/use-state constants/default-color)
        on-color-change               (fn [new-color]
                                        (println :changed new-color)
                                        (set-color-value new-color)
                                        (change-color new-color))]
    [:<>
     [rn/view {:style {:flex 1 :padding-right 8}}
      [quo/color-picker
       {:default-selected color-value
        :on-change        on-color-change}]]
     [chat.composer/submit-button
      {:on-submit #(submit-color color-value)
       :color     color-value}]]))

(def color-step
  {:message  message
   :composer composer})
