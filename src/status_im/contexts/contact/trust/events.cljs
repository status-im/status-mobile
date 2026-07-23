(ns status-im.contexts.contact.trust.events
  (:require
    [status-im.common.confirmation-drawer.view :as confirmation-drawer]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :contact/mark-as-untrusted-success
 (fn [{:keys [db]} [contact-id]]
   {:db (update-in db
                   [:contacts/contacts contact-id]
                   assoc
                   :trust-status
                   constants/contact-trust-status-untrustworthy)
    :fx [[:dispatch
          [:toasts/upsert
           {:type :positive
            :text (i18n/label :t/marked-as-untrusted)}]]]}))

(rf/reg-event-fx :contact/mark-as-untrusted
 (fn [_ [contact-id name]]
   {:json-rpc/call
    [{:method     "wakuext_markAsUntrustworthy"
      :params     [contact-id]
      :on-success [:contact/mark-as-untrusted-success contact-id name]
      :on-error   #(log/error "failed mark contact as untrusted"
                              {:event      :contact/mark-as-untrusted
                               :contact-id contact-id
                               :error      %})}]}))

(rf/reg-event-fx :contact/remove-trust-status-success
 (fn [{:keys [db]} [contact-id]]
   {:db (update-in db
                   [:contacts/contacts contact-id]
                   assoc
                   :trust-status
                   constants/contact-trust-status-unknown)
    :fx [[:dispatch
          [:toasts/upsert
           {:type :positive
            :text (i18n/label :t/untrusted-mark-removed)}]]]}))

(rf/reg-event-fx :contact/remove-trust-status
 (fn [_ [contact-id name]]
   {:json-rpc/call
    [{:method     "wakuext_removeTrustStatus"
      :params     [contact-id]
      :on-success [:contact/remove-trust-status-success contact-id name]
      :on-error   #(log/error "failed remove contact trust status"
                              {:event      :contact/remove-trust-status
                               :contact-id contact-id
                               :error      %})}]}))

(defn pending-contact-request-from-contact-id
  [db contact-id]
  (->> (get-in db [:activity-center :contact-requests])
       (filter #(= contact-id (:author %)))
       first))

(rf/reg-event-fx :contact/mark-as-untrusted-sheet
 (fn [{:keys [db]} [{:keys [public-key contact-request-state] :as contact}]]
   (let [name            (profile.utils/displayed-name contact)
         contact?        (= contact-request-state
                            constants/contact-request-state-mutual)
         request?        (= contact-request-state
                            constants/contact-request-state-received)
         contact-request (when request?
                           (pending-contact-request-from-contact-id db public-key))]
     {:fx [[:dispatch
            [:show-bottom-sheet
             {:content (fn []
                         [confirmation-drawer/confirmation-drawer
                          (cond-> {:title               (i18n/label :t/mark-as-untrusted)
                                   :description         (i18n/label :t/mark-as-untrusted-description
                                                                    {:username (:primary-name contact)})
                                   :context             contact
                                   :accessibility-label :mark-as-untrustworthy
                                   :button-text         (i18n/label :t/mark-as-untrusted-button)
                                   :on-press            (fn []
                                                          (rf/dispatch [:hide-bottom-sheet])
                                                          (rf/dispatch
                                                           [:contact/mark-as-untrusted
                                                            public-key name]))}
                            contact?
                            (assoc :extra-action (fn []
                                                   (rf/dispatch [:contact.ui/remove-contact-pressed
                                                                 contact]))
                                   :extra-text   (i18n/label :t/remove-contact))
                            request?
                            (assoc :extra-action (fn []
                                                   (rf/dispatch
                                                    [:activity-center.contact-requests/decline
                                                     (:id contact-request)]))
                                   :extra-text   (i18n/label :t/decline-contact-request)))])}]]]})))
