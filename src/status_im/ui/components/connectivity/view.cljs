(ns status-im.ui.components.connectivity.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.i18n :as i18n]))

(defview error-view [{:keys [top]}]
  (letsubs [offline?             [:offline?]
            disconnected?        [:disconnected?]
            mailserver-error?    [:mailserver-error?]
            fetching?            [:fetching?]
            current-chat-contact [:get-current-chat-contact]
            view-id              [:get :view-id]
            window-width         [:dimensions/window-width]]
    (when-let [label (cond
                       offline? :t/offline
                       disconnected? :t/disconnected
                       mailserver-error? :t/mailserver-reconnect
                       fetching? :t/fetching-messages
                       :else nil)]
      (let [pending? (and (:pending current-chat-contact) (= :chat view-id))]
        [react/view {:style               (styles/text-wrapper top 1.0 window-width pending?)
                     :accessibility-label :connection-status-text}
         [react/text {:style    styles/text
                      :on-press (when mailserver-error?
                                  #(re-frame/dispatch [:inbox/reconnect]))}
          (i18n/label label)]]))))
