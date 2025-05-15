(ns status-im.contexts.contact.trust.events
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [status-im.common.confirmation-drawer.view :as confirmation-drawer]
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :contact/mark-as-untrusted-success
 (fn [{:keys [db]} [contact-id name]]
   {:db (update-in db
                   [:contacts/contacts contact-id]
                   #(assoc % :trust-status constants/contact-trust-status-untrustworthy))
    :dispatch
    [:toasts/upsert
     {:type :positive
      :text (i18n/label :t/marked-as-untrusted {:username name})}]}))

(rf/reg-event-fx :contact/mark-as-untrusted
 (fn [_ [contact-id name]]
   {:json-rpc/call
    [{:method     "wakuext_markAsUntrustworthy"
      :params     [contact-id]
      :on-success #(re-frame/dispatch [:contact/mark-as-untrusted-success contact-id name])
      :on-error   #(log/error "failed mark contact as untrusted" % contact-id)}]}))

(rf/reg-event-fx :contact/remove-trust-status-success
 (fn [{:keys [db]} [contact-id name]]
   {:db (update-in db
                   [:contacts/contacts contact-id]
                   #(assoc % :trust-status constants/contact-trust-status-unknown))
    :dispatch
    [:toasts/upsert
     {:type :positive
      :text (i18n/label :t/trust-mark-removed {:username name})}]}))

(rf/reg-event-fx :contact/remove-trust-status
 (fn [_ [contact-id name]]
   {:json-rpc/call
    [{:method     "wakuext_removeTrustStatus"
      :params     [contact-id]
      :on-success #(re-frame/dispatch [:contact/remove-trust-status-success contact-id name])
      :on-error   #(log/error "failed remove contact trust status" % contact-id)}]}))

(rf/reg-event-fx :contact/mark-as-untrusted-sheet
 (fn [_ [{:keys [public-key primary-name] :as item}]]
   {:dispatch
    [:show-bottom-sheet
     {:content (fn []
                 [confirmation-drawer/confirmation-drawer
                  {:title               (i18n/label :t/mark-as-untrusted)
                   :description         (i18n/label :t/mark-as-untrusted-description
                                                    {:username (:primary-name item)})
                   :extra-action        (fn []
                                          (rf/dispatch [:toasts/upsert
                                                        {:id   :remove-contact
                                                         :type :positive
                                                         :text (->> (i18n/label :t/removed-from-contacts)
                                                                    (string/lower-case)
                                                                    (str primary-name " "))}])
                                          (rf/dispatch [:contact.ui/remove-contact-pressed item]))
                   :extra-text          (i18n/label :t/remove-contact)
                   :context             item
                   :accessibility-label :mark-as-untrustworthy
                   :button-text         (i18n/label :t/mark-as-untrusted-button)
                   :on-press            (fn []
                                          (rf/dispatch [:hide-bottom-sheet])
                                          (rf/dispatch
                                           [:contact/mark-as-untrusted
                                            public-key primary-name]))}])}]}))
