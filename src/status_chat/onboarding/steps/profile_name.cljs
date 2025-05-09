(ns status-chat.onboarding.steps.profile-name
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-chat.components.chat.composer :as chat.composer]
            [status-chat.components.chat.message :as chat.message]
            [utils.re-frame :as rf]))

(defn on-submit
  [value]
  (rf/dispatch [:onboarding/submit-reply :name {:input-value value}]))

(defn message
  [{:keys [reply]}]
  [:<>
   [chat.message/text "Btw, didn't catch your name"]
   (when reply
     [chat.message/extra-content {:on-press identity}
      [chat.message/text (:input-value reply)]])])

(defn composer
  []
  (let [color                       (rf/sub [:onboarding/color])
        [name-value set-name-value] (rn/use-state "")]
    [:<>
     [rn/view {:style {:flex 1 :padding-right 8}}
      [quo/input
       {:on-change-text      #(set-name-value %)
        :value               name-value
        :customization-color color
        :auto-focus          true}]]
     [chat.composer/submit-button
      {:on-submit #(on-submit name-value)
       :color     color}]]))


(def name-step
  {:message  message
   :composer composer})
