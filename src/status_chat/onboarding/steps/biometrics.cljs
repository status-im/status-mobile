(ns status-chat.onboarding.steps.biometrics
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-chat.components.chat.message :as chat.message]
            [utils.re-frame :as rf]))

(defn on-submit
  []
  (rf/dispatch
   [:biometric/authenticate
    {:on-success #(rf/dispatch [:onboarding/submit-reply :biometrics {:enabled? true}])
     :on-fail    #(rf/dispatch [:onboarding/biometrics-fail %])}]))

(defn on-skip
  []
  (rf/dispatch [:onboarding/submit-reply :biometrics {:enabled? false}]))

(defn biometrics-message
  [{:keys [reply]}]
  [:<>
   [chat.message/text
    "We know you don't want to type your password every time. Go ahead and enable biometrics if you want"]
   (when reply
     (if (:enabled? reply)
       [chat.message/extra-content {:on-press on-skip}
        [quo/icon :i/face-id {:size 20}]]
       [chat.message/extra-content {:on-press on-submit}
        [chat.message/text "Skipped. Try again?"]]))])

(defn biometrics-composer
  []
  (let [color (rf/sub [:onboarding/color])]
    [rn/view
     {:style {:flex-direction  :row
              :justify-content :center
              :align-items     :center}}
     [rn/view {:style {:flex 1 :margin-right 8}}
      [quo/button
       {:size                40
        :accessibility-label :enable-biometrics-button
        :icon-left           :i/face-id
        :customization-color color
        :on-press            on-submit}
       "Sure"]]
     [quo/button
      {:accessibility-label :maybe-later-button
       :background          :blur
       :type                :outline
       :on-press            on-skip}
      "Maybe later"]]))

(def biometrics-step
  {:message  biometrics-message
   :composer biometrics-composer})
