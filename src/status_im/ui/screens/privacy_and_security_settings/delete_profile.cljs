(ns status-im.ui.screens.privacy-and-security-settings.delete-profile
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            status-im.keycard.delete-key
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.privacy-and-security-settings.events :as delete-profile]
            [utils.security.core :as security]))

(defn valid-password?
  [password]
  (>= (count password) 6))

(defn on-delete-profile
  [password]
  #(do
     (re-frame/dispatch
      [::delete-profile/delete-profile @password])
     (reset! password nil)))

(defn on-delete-keycard-profile
  [keep-keys-on-keycard?]
  #(re-frame/dispatch
    [:keycard/proceed-to-reset-card keep-keys-on-keycard?]))

(defn delete-profile
  []
  (let [password       (reagent/atom nil)
        text-input-ref (atom nil)]
    (fn []
      (let [keycard?              @(re-frame/subscribe [:keycard-multiaccount?])
            multiaccount          @(re-frame/subscribe [:multiaccount])
            error                 @(re-frame/subscribe [:delete-profile/error])
            keep-keys-on-keycard? @(re-frame/subscribe [:delete-profile/keep-keys-on-keycard?])]
        (when (and @text-input-ref error (not @password))
          (.clear ^js @text-input-ref))
        [react/keyboard-avoiding-view {:style {:flex 1}}
         [react/scroll-view {:style {:flex 1}}
          [react/view {:style {:align-items :center}}
           [quo/text
            {:weight :bold
             :size   :x-large}
            (i18n/label :t/delete-profile)]]
          [quo/list-item
           {:title (multiaccounts/displayed-name multiaccount)
            :icon  [chat-icon.screen/contact-icon-contacts-tab
                    (multiaccounts/displayed-photo multiaccount)]}]
          (when keycard?
            [react/view
             [quo/list-header (i18n/label :t/actions)]
             [quo/list-item
              {:title     (i18n/label :t/delete-keys-keycard)
               :accessory :checkbox
               :active    (not keep-keys-on-keycard?)
               :on-press  #(re-frame/dispatch [::delete-profile/keep-keys-on-keycard
                                               (not keep-keys-on-keycard?)])}]
             [quo/list-item
              {:title              (i18n/label :t/unpair-keycard)
               :subtitle           (i18n/label :t/unpair-keycard-warning)
               :subtitle-max-lines 4
               :disabled           true
               :active             true
               :accessory          :checkbox}]
             [quo/list-item
              {:title              (i18n/label :t/reset-database)
               :subtitle           (i18n/label :t/reset-database-warning-keycard)
               :subtitle-max-lines 4
               :disabled           true
               :active             true
               :accessory          :checkbox}]])
          (when-not keycard?
            [quo/text
             {:style {:margin-horizontal 24}
              :align :center
              :color :negative}
             (i18n/label :t/delete-profile-warning)])
          (when-not keycard?
            [quo/text-input
             {:style             {:margin-horizontal 36
                                  :margin-top        36}
              :show-cancel       false
              :secure-text-entry true
              :return-key-type   :next
              :on-submit-editing nil
              :auto-focus        true
              :on-change-text    #(reset! password (security/mask-data %))
              :bottom-value      36
              :get-ref           #(reset! text-input-ref %)
              :error             (when (and error (not @password))
                                   (if (= :wrong-password error)
                                     (i18n/label :t/wrong-password)
                                     (str error)))}])]
         [react/view {:style {:align-items :center}}
          (when-not keycard?
            [quo/separator])
          (when (and keycard? (not keep-keys-on-keycard?))
            [quo/text
             {:style {:margin-horizontal 24 :margin-bottom 16}
              :align :center
              :color :negative}
             (i18n/label :t/delete-profile-warning)])
          [react/view
           {:style {:margin-vertical 8}}
           [quo/button
            {:on-press            (if keycard?
                                    (on-delete-keycard-profile keep-keys-on-keycard?)
                                    (on-delete-profile password))
             :theme               :negative
             :accessibility-label :delete-profile-confirm
             :disabled            (and (not keycard?) ((complement valid-password?) @password))}
            (i18n/label :t/delete-profile)]]]]))))
