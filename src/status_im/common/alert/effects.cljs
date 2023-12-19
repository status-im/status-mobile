(ns status-im.common.alert.effects
  (:require
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn show-popup
  ([title content]
   (show-popup title content nil nil))
  ([title content on-dismiss]
   (show-popup title content on-dismiss nil))
  ([title content on-dismiss action-button]
   (let [dismiss-button
         (merge {:text                "OK"
                 :style               "cancel"
                 :accessibility-label :cancel-button}
                (when on-dismiss {:onPress on-dismiss}))]
     (rn/alert
      title
      content
      (if action-button
        (vector
         action-button
         dismiss-button)
        (vector dismiss-button))
      (when on-dismiss
        {:cancelable false})))))

(rf/reg-fx :effects.utils/show-popup
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

(rf/reg-fx :effects.utils/show-confirmation
 (fn [{:keys [title content confirm-button-text on-accept on-cancel cancel-button-text extra-options]}]
   (show-confirmation {:title               title
                       :content             content
                       :confirm-button-text confirm-button-text
                       :cancel-button-text  cancel-button-text
                       :on-accept           on-accept
                       :on-cancel           on-cancel
                       :extra-options       extra-options})))
