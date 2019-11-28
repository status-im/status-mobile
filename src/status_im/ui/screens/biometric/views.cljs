(ns status-im.ui.screens.biometric.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.i18n :as i18n]))

(defn get-supported-biometric-auth []
  @(re-frame/subscribe [:supported-biometric-auth]))

(defn get-bio-type-label []
  (biometric/get-label (get-supported-biometric-auth)))

(defn biometric-popover
  [{:keys [title-label description-label description-text
           ok-button-label cancel-button-label on-cancel on-confirm]}]
  (let [supported-biometric-auth (get-supported-biometric-auth)
        bio-type-label           (get-bio-type-label)]
    [react/view {:padding     24
                 :align-items :center}
     [react/view {:margin-bottom    16
                  :width            32
                  :height           32
                  :background-color colors/blue-light
                  :border-radius    16
                  :align-items      :center
                  :justify-content  :center}
      [icons/icon (if (= supported-biometric-auth :FaceID) :faceid :print)]]

     [react/text {:style {:typography :title-bold}}
      (str (i18n/label title-label {:bio-type-label bio-type-label}))]
     (vec
      (concat
       [react/nested-text {:style {:margin-bottom 25
                                   :margin-top    10
                                   :text-align    :center}}]
       (if description-label
         [(i18n/label description-label {:bio-type-label bio-type-label})]
         description-text)))
     [button/button {:label    (i18n/label ok-button-label
                                           {:bio-type-label bio-type-label})
                     :style    {:margin-bottom 16}
                     :on-press #(re-frame/dispatch [on-confirm])}]
     [button/button {:label    (or cancel-button-label :t/cancel)
                     :type     :secondary
                     :on-press #(re-frame/dispatch [(or on-cancel :hide-popover)])}]]))

(defn disable-password-saving-popover []
  (let [bio-label-type (get-bio-type-label)]
    [biometric-popover
     {:title-label     :t/biometric-disable-password-title
      :ok-button-label :t/continue
      :on-confirm      :biometric/disable

      :description-text
      [[{:style {:color colors/gray}}
        (i18n/label :t/biometric-disable-password-description)]
       [{} (i18n/label :t/biometric-disable-bioauth
                       {:bio-type-label bio-label-type})]]}]))

(defn enable-biometric-popover []
  [biometric-popover
   {:title-label       :t/enable
    :description-label :t/to-enable-biometric
    :ok-button-label   :t/biometric-enable-button
    :on-confirm        :biometric-logout}])

(defn secure-with-biometric-popover []
  (let [bio-label-type (get-bio-type-label)]
    [biometric-popover
     {:title-label     :t/biometric-secure-with
      :ok-button-label :t/biometric-enable-button
      :on-confirm      :biometric/enable

      :description-text
      [[{:style {:color colors/gray}} (i18n/label :t/biometric-enable)]
       [{} (i18n/label :t/biometric-sign-in {:bio-type-label bio-label-type})]]}]))
