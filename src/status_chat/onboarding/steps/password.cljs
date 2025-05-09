(ns status-chat.onboarding.steps.password
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-chat.components.chat.composer :as chat.composer]
            [status-chat.components.chat.message :as chat.message]
            [utils.re-frame :as rf]))

(defn password-reply
  [reply]
  [chat.message/extra-content {:on-press identity}
   [chat.message/hidden-text (:input-value reply)]])

(defn create-password-message
  [{:keys [reply]}]
  [:<>
   [chat.message/text
    "Hey, so pretty sure you want to protect your profile. Wanna write down your new password?"]
   (when reply
     [password-reply reply])])

(defn repeat-password-message
  [{:keys [reply]}]
  [:<>
   [chat.message/text
    "Write it once again ... just in case"]
   (when reply
     [password-reply reply])])

(defn password-composer-input
  [props]
  (let [color (rf/sub [:onboarding/color])]
    [rn/view {:style {:flex 1 :padding-right 8}}
     [quo/input
      (assoc props
             :customization-color color
             :auto-focus          true
             :type                :password)]]))

(defn- on-submit
  [step value]
  (rf/dispatch [:onboarding/submit-reply step {:input-value value}]))

(defn create-password-composer
  []
  (let [[password-value set-password-value] (rn/use-state "")
        color                               (rf/sub [:onboarding/color])]
    [:<>
     [password-composer-input
      {:on-change-text #(set-password-value %)
       :value          password-value}]
     [chat.composer/submit-button
      {:on-submit #(on-submit :create-password password-value)
       :color     color}]]))

(defn repeat-password-composer
  []
  (let [[password-value set-password-value] (rn/use-state "")
        [error? set-error?]                 (rn/use-state false)
        create-password                     (rf/sub [:onboarding/reply-for-step :create-password])
        color                               (rf/sub [:onboarding/color])
        on-change-value                     (fn [value]
                                              (when error?
                                                (set-error? false))
                                              (set-password-value value))
        on-press-submit                     (fn []
                                              (if (= (:input-value create-password) password-value)
                                                (on-submit :repeat-password password-value)
                                                (set-error? true)))]
    [:<>
     [password-composer-input
      {:on-change-text on-change-value
       :error?         error?
       :value          password-value}]
     [chat.composer/submit-button
      {:on-submit on-press-submit
       :color     color}]]))

(def create-password-step
  {:message  create-password-message
   :composer create-password-composer})

(def repeat-password-step
  {:message  repeat-password-message
   :composer repeat-password-composer})
