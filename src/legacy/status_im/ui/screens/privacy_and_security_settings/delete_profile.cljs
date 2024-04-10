(ns legacy.status-im.ui.screens.privacy-and-security-settings.delete-profile
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon.screen]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.privacy-and-security-settings.events :as delete-profile]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
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

(defn delete-profile
  []
  (let [password       (reagent/atom nil)
        text-input-ref (atom nil)]
    (fn []
      (let [profile               @(re-frame/subscribe [:profile/profile])
            error                 @(re-frame/subscribe [:delete-profile/error])
            keep-keys-on-keycard? @(re-frame/subscribe
                                    [:delete-profile/keep-keys-on-keycard?])]
        (when (and @text-input-ref error (not @password))
          (.clear ^js @text-input-ref))
        [react/keyboard-avoiding-view {:style {:flex 1}}
         [react/scroll-view {:style {:flex 1}}
          [react/view {:style {:align-items :center}}
           [quo/text
            {:weight :bold
             :size   :x-large}
            (i18n/label :t/delete-profile)]]
          [list.item/list-item
           {:title (profile.utils/displayed-name profile)
            :icon  [chat-icon.screen/contact-icon-contacts-tab profile]}]
          [quo/text
           {:style {:margin-horizontal 24}
            :align :center
            :color :negative}
           (i18n/label :t/delete-profile-warning)]
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
                                   (str error)))}]]
         [react/view {:style {:align-items :center}}
          [quo/separator]
          (when (not keep-keys-on-keycard?)
            [quo/text
             {:style {:margin-horizontal 24 :margin-bottom 16}
              :align :center
              :color :negative}
             (i18n/label :t/delete-profile-warning)])
          [react/view
           {:style {:margin-vertical 8}}
           [quo/button
            {:on-press            (on-delete-profile password)
             :theme               :negative
             :accessibility-label :delete-profile-confirm
             :disabled            ((complement valid-password?) @password)}
            (i18n/label :t/delete-profile)]]]]))))
