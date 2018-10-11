(ns status-im.ui.components.connectivity.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.i18n :as i18n]))

(defview error-label
  [{:keys [view-id label fetching? mailserver-error?] :as opts}]

  {:should-component-update
   (fn [_ [_ old-props] [_ new-props]]
     ;; prevents flickering on navigation
     (= (:view-id old-props) (:view-id new-props)))}
  (let [wrapper-style (styles/text-wrapper
                       (assoc opts :modal? (= view-id :chat-modal)))]
    [react/view {:style               wrapper-style
                 :accessibility-label :connection-status-text}
     [react/text {:style    styles/text
                  :on-press (when mailserver-error?
                              #(re-frame/dispatch [:inbox.ui/reconnect-mailserver-pressed]))}
      (if fetching?
        (i18n/label :t/fetching-messages {:requests-left (str fetching?)})
        (i18n/label label))]]))

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
        [error-label
         {:view-id           view-id
          :top               top
          :window-width      window-width
          :pending?          pending?
          :label             label
          :fetching?         fetching?
          :mailserver-error? mailserver-error?}]))))
