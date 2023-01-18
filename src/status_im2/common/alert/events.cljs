(ns status-im2.common.alert.events
  (:require [utils.i18n :as i18n]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]))

(defn show-popup
  ([title content]
   (show-popup title content nil))
  ([title content on-dismiss]
   (rn/alert
    title
    content
    (vector (merge {:text                "OK"
                    :style               "cancel"
                    :accessibility-label :cancel-button}
                   (when on-dismiss {:onPress on-dismiss})))
    (when on-dismiss
      {:cancelable false}))))

(re-frame/reg-fx
 :utils/show-popup
 (fn [{:keys [title content on-dismiss]}]
   (show-popup title content on-dismiss)))

(defn show-confirmation
  [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text
           extra-options]}]
  (rn/alert
   title
   content
   ;; Styles are only relevant on iOS. On Android first button is 'neutral' and second is 'positive'
   (concat
    (vector (merge {:text                (or cancel-button-text (i18n/label :t/cancel))
                    :style               "cancel"
                    :accessibility-label :cancel-button}
                   (when on-cancel {:onPress on-cancel}))
            {:text                (or confirm-button-text (i18n/label :t/ok))
             :onPress             on-accept
             :style               "default"
             :accessibility-label :confirm-button})
    (or extra-options nil))
   {:cancelable false}))

(re-frame/reg-fx
 :utils/show-confirmation
 (fn [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text extra-options]}]
   (show-confirmation {:title               title
                       :content             content
                       :confirm-button-text confirm-button-text
                       :cancel-button-text  cancel-button-text
                       :on-accept           on-accept
                       :on-cancel           on-cancel
                       :extra-options       extra-options})))

(defn show-question
  ([title content on-accept]
   (show-question title content on-accept nil))
  ([title content on-accept on-cancel]
   (rn/alert
    title
    content
    (vector (merge {:text                (i18n/label :t/no)
                    :accessibility-label :no-button}
                   (when on-cancel {:onPress on-cancel}))
            {:text                (i18n/label :t/yes)
             :onPress             on-accept
             :accessibility-label :yes-button})
    nil)))
