(ns status-im.ui.screens.biometric.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]))

(defn get-supported-biometric-auth
  []
  @(re-frame/subscribe [:supported-biometric-auth]))

(defn get-bio-type-label
  []
  (biometric/get-label (get-supported-biometric-auth)))

(defn biometric-popover
  [{:keys [title-label description-label description-text
           ok-button-label cancel-button-label on-cancel on-confirm]}]
  (let [supported-biometric-auth (get-supported-biometric-auth)
        bio-type-label           (get-bio-type-label)]
    [react/view
     {:margin-top         24
      :align-items        :center
      :padding-horizontal 24}
     [react/view
      {:width            32
       :height           32
       :background-color colors/blue-light
       :border-radius    16
       :align-items      :center
       :justify-content  :center}
      [icons/icon
       (if (= supported-biometric-auth :FaceID)
         :main-icons/faceid
         :main-icons/print)
       {:color colors/blue}]]

     [react/text
      {:style {:typography :title-bold
               :margin-top 16}}
      (str (i18n/label title-label {:bio-type-label bio-type-label}))]
     (vec
      (concat
       [react/nested-text
        {:style {:margin-top 8
                 :color      colors/gray
                 :text-align :center}}]
       (if description-label
         [(i18n/label description-label {:bio-type-label bio-type-label})]
         description-text)))
     [react/view {:padding-vertical 16}
      [react/view {:padding-vertical 8}
       [quo/button {:on-press #(re-frame/dispatch [on-confirm])}
        (i18n/label ok-button-label
                    {:bio-type-label bio-type-label})]]
      [quo/button
       {:type     :secondary
        :on-press #(re-frame/dispatch [(or on-cancel :hide-popover)])}
       (or cancel-button-label
           (i18n/label :t/cancel))]]]))

(defn disable-password-saving-popover
  []
  (let [bio-label-type (get-bio-type-label)]
    [biometric-popover
     {:title-label      :t/biometric-disable-password-title
      :ok-button-label  :t/continue
      :on-confirm       :biometric/disable

      :description-text
      [[{:style {:color colors/gray}}
        (i18n/label :t/biometric-disable-password-description)]
       [{}
        (i18n/label :t/biometric-disable-bioauth
                    {:bio-type-label bio-label-type})]]}]))

(defn enable-biometric-popover
  []
  [biometric-popover
   {:title-label       :t/biometric-secure-with
    :description-label :t/to-enable-biometric
    :ok-button-label   :t/biometric-enable-button
    :on-confirm        :biometric-logout}])

(defn secure-with-biometric-popover
  []
  (let [keycard-account? @(re-frame/subscribe
                           [:multiaccounts.login/keycard-account?])]
    [biometric-popover
     {:title-label       :t/biometric-secure-with
      :ok-button-label   :t/biometric-enable-button
      :on-confirm        :biometric/enable
      :description-label (if keycard-account?
                           :t/biometric-enable-keycard
                           :t/biometric-enable)}]))
