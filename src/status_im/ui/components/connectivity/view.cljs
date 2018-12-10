(ns status-im.ui.components.connectivity.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.utils.platform :as utils.platform]
            [status-im.i18n :as i18n]))

(defview error-label
  [{:keys [view-id label mailserver-fetching? mailserver-connection-error?
           mailserver-request-error?] :as opts}]
  {:should-component-update
   (fn [_ [_ old-props] [_ new-props]]
     ;; prevents flickering on navigation
     (= (:view-id old-props) (:view-id new-props)))}
  (let [wrapper-style (styles/text-wrapper
                       (assoc opts :modal? (= view-id :chat-modal)))]
    [react/view {:style               wrapper-style
                 :accessibility-label :connection-status-text}
     [react/text {:style    styles/text
                  :on-press #(cond
                               mailserver-connection-error?
                               (re-frame/dispatch [:mailserver.ui/reconnect-mailserver-pressed])
                               mailserver-request-error?
                               (re-frame/dispatch [:mailserver.ui/request-error-pressed]))}
      (if (and (not (or mailserver-connection-error?
                        mailserver-request-error?))
               mailserver-fetching?)
        (i18n/label :t/fetching-messages {:requests-left (str mailserver-fetching?)})
        (i18n/label label))]]))

(defview error-view [{:keys [top]}]
  (letsubs [offline?                     [:offline?]
            disconnected?                [:disconnected?]
            mailserver-connection-error? [:mailserver/connection-error?]
            mailserver-request-error?    [:mailserver/request-error?]
            mailserver-fetching?         [:mailserver/fetching?]
            current-chat-contact         [:chats/current-chat-contact]
            view-id                      [:get :view-id]
            window-width                 [:dimensions/window-width]]
    (let [wallet-offline? (and offline?
                               ;; There's no wallet of desktop
                               (not utils.platform/desktop?))]

      (when-let [label (cond
                         (and wallet-offline?
                              disconnected?) :t/offline

                         wallet-offline? :t/wallet-offline
                         disconnected? :t/disconnected

                         mailserver-connection-error? :t/mailserver-reconnect
                         mailserver-request-error? :t/mailserver-request-error-status
                         mailserver-fetching? :t/fetching-messages
                         :else nil)]
        (let [pending? (and (:pending current-chat-contact) (= :chat view-id))]
          [error-label
           {:view-id                      view-id
            :top                          top
            :window-width                 window-width
            :pending?                     pending?
            :label                        label
            :mailserver-fetching?         mailserver-fetching?
            :mailserver-request-error?    mailserver-request-error?
            :mailserver-connection-error? mailserver-connection-error?}])))))
