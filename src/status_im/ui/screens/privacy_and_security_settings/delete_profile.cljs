(ns status-im.ui.screens.privacy-and-security-settings.delete-profile
  (:require [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [re-frame.core :as re-frame]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n.i18n :as i18n]
            [reagent.core :as reagent]
            [status-im.utils.security :as security]
            [status-im.ui.screens.privacy-and-security-settings.events :as delete-profile]))

(defn valid-password? [password]
  (>= (count password) 6))

(defn keycard-pin []
  #_(let [pin             @(re-frame/subscribe [:keycard/pin])
          step              @(re-frame/subscribe [:keycard/pin-enter-step])
          status            @(re-frame/subscribe [:keycard/pin-status])
          pin-retry-counter @(re-frame/subscribe [:keycard/pin-retry-counter])
          puk-retry-counter @(re-frame/subscribe [:keycard/puk-retry-counter])
          error-label       @(re-frame/subscribe [:keycard/pin-error-label])]
      [pin.views/pin-view
       {:pin           pin
        :status        status
        :retry-counter pin-retry-counter
        :error-label   error-label
        :step          :current}]))

(defn delete-profile []
  (let [password       (reagent/atom nil)
        text-input-ref (atom nil)]
    (fn []
      (let [keycard?     @(re-frame/subscribe [:keycard-multiaccount?])
            multiaccount @(re-frame/subscribe [:multiaccount])
            error        @(re-frame/subscribe [:delete-profile/error])]
        (when (and @text-input-ref error (not @password))
          (.clear ^js @text-input-ref))
        [react/view {:flex 1}
         [topbar/topbar {:modal? true}]
         [react/view
          {:style {:flex            1
                   :justify-content :space-between}}
          [react/scroll-view {:style {:flex 1}}
           [react/view {:style {:align-items :center}}
            [quo/text {:weight :bold
                       :size   :x-large}
             (i18n/label :t/delete-profile)]]
           [quo/list-item
            {:title   (multiaccounts/displayed-name multiaccount)
             :icon    [chat-icon.screen/contact-icon-contacts-tab
                       (multiaccounts/displayed-photo multiaccount)]}]
           [quo/text {:style {:margin-horizontal 24}
                      :align :center
                      :color :negative}
            (i18n/label :t/delete-profile-warning)]
           (if keycard?
             [keycard-pin]
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
          (when-not keycard?
            [react/view {:style {:align-items :center}}
             [quo/separator]
             [react/view
              {:style {:margin-vertical 8}}
              [quo/button {:on-press #(do
                                        (re-frame/dispatch
                                         [::delete-profile/delete-profile @password])
                                        (reset! password nil))
                           :theme               :negative
                           :accessibility-label :delete-profile-confirm
                           :disabled ((complement valid-password?) @password)}
               (i18n/label :t/delete-profile)]]])]]))))
