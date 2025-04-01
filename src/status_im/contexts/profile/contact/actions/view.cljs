(ns status-im.contexts.profile.contact.actions.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.config :as config]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.add-nickname.view :as add-nickname]
            [status-im.contexts.profile.contact.block-contact.view :as block-contact]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-add-nickname
  []
  (rf/dispatch [:show-bottom-sheet
                {:content
                 (fn [] [add-nickname/view])}]))

(defn on-block-contact
  []
  (rf/dispatch [:show-bottom-sheet
                {:content
                 (fn [] [block-contact/view])}]))

(defn view
  []
  (let [{:keys [nickname public-key contact-request-state blocked?]
         :as   contact}    (rf/sub [:contacts/current-contact])
        full-name          (profile.utils/displayed-name contact)
        on-remove-nickname (rn/use-callback
                            (fn []
                              (rf/dispatch [:hide-bottom-sheet])
                              (rf/dispatch [:toasts/upsert
                                            {:id   :remove-nickname
                                             :type :positive
                                             :text (i18n/label :t/nickname-removed)}])
                              (rf/dispatch [:contacts/update-nickname public-key ""]))
                            [public-key])
        on-show-qr         (rn/use-callback
                            (fn []
                              (rf/dispatch [:universal-links/generate-profile-url
                                            {:public-key public-key
                                             :on-success #(rf/dispatch [:open-modal
                                                                        :screen/share-contact])}]))
                            [public-key])
        has-nickname?      (rn/use-memo (fn [] (not (string/blank? nickname))) [nickname])
        on-share-profile   (rn/use-callback
                            (fn []
                              (rf/dispatch [:universal-links/generate-profile-url
                                            {:public-key public-key
                                             :on-success #(rf/dispatch [:open-share
                                                                        {:options {:message %}}])}]))
                            [public-key])
        on-remove-contact  (rn/use-callback
                            (fn []
                              (rf/dispatch [:hide-bottom-sheet])
                              (rf/dispatch [:toasts/upsert
                                            {:id   :remove-contact
                                             :type :positive
                                             :text (->> (i18n/label :t/removed-from-contacts)
                                                        (string/lower-case)
                                                        (str full-name " "))}])
                              (rf/dispatch [:contact.ui/remove-contact-pressed contact]))
                            [public-key full-name])]
    [quo/action-drawer
     [(concat
       [{:icon                :i/edit
         :label               (if has-nickname?
                                (i18n/label :t/edit-nickname)
                                (i18n/label :t/add-nickname-title))
         :on-press            on-add-nickname
         :accessibility-label (if nickname :edit-nickname :add-nickname)}]
       (when-not blocked?
         [{:icon                :i/qr-code
           :label               (i18n/label :t/show-qr)
           :on-press            on-show-qr
           :accessibility-label :show-qr-code}
          {:icon                :i/share
           :label               (i18n/label :t/share-profile)
           :on-press            on-share-profile
           :accessibility-label :share-profile}
          (when has-nickname?
            {:icon                :i/delete
             :label               (i18n/label :t/remove-nickname)
             :on-press            on-remove-nickname
             :add-divider?        true
             :accessibility-label :remove-nickname
             :danger?             true})
          (when config/show-not-implemented-features?
            {:icon                :i/untrustworthy
             :label               (i18n/label :t/mark-untrustworthy)
             :on-press            not-implemented/alert
             :accessibility-label :mark-untrustworthy
             :add-divider?        (when-not has-nickname? true)
             :danger?             true})
          (when (= constants/contact-request-state-mutual contact-request-state)
            {:icon                :i/remove-user
             :label               (i18n/label :t/remove-contact)
             :on-press            on-remove-contact
             :accessibility-label :remove-contact
             :danger?             true})
          {:icon                :i/block
           :label               (i18n/label :t/block-user)
           :on-press            on-block-contact
           :accessibility-label :block-user
           :danger?             true}]))]]))
