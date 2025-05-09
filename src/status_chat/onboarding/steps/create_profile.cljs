(ns status-chat.onboarding.steps.create-profile
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-chat.components.chat.message :as chat.message]
            [utils.re-frame :as rf]))

(defn message
  [{:keys [reply]}]
  (println :reply reply)
  [:<>
   [chat.message/text "Now we're going to create a profile for you"]

   (when (and (not (:loading? reply))
              (not (nil? reply)))
     [chat.message/extra-content
      [rn/view {:style {:height 40}}
       [quo/icon :i/done {:size 20}]]])])

(defn composer
  []
  (let [color (rf/sub [:onboarding/color])
        reply (rf/sub [:onboarding/reply-for-step :create-profile])]
    (cond
      (nil? reply)
      [rn/view {:flex 1}
       [quo/slide-button
        {:size                :size-48
         :track-text          "Create profile"
         :container-style     {:z-index 2}
         :track-icon          :i/send
         :customization-color color
         :on-complete         #(rf/dispatch
                                [:onboarding/create-profile])}]]
      (:loading? reply)
      [rn/activity-indicator]

      (not (:loading? reply))
      [quo/button
       {:size                40
        :customization-color color
        :on-press            #(rf/dispatch [:onboarding/login])}
       "Let me in!"])))

(def step
  {:message  message
   :composer composer})
