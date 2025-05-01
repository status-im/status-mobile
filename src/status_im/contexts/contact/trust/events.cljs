(ns status-im.contexts.contact.trust.events
  (:require
    [re-frame.core :as re-frame]
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
