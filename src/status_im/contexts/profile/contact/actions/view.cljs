(ns status-im.contexts.profile.contact.actions.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.profile.contact.add-nickname.view :as add-nickname]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [nickname public-key]} (rf/sub [:contacts/current-contact])
        on-add-nickname               (rn/use-callback #(rf/dispatch [:show-bottom-sheet
                                                                      {:content
                                                                       (fn [] [add-nickname/view])}]))
        on-remove-nickname            (rn/use-callback
                                       (fn []
                                         (rf/dispatch [:hide-bottom-sheet])
                                         (rf/dispatch [:toasts/upsert
                                                       {:id   :remove-nickname
                                                        :type :positive
                                                        :text (i18n/label :t/nickname-removed)}])
                                         (rf/dispatch [:contacts/update-nickname public-key ""]))
                                       [public-key])
        on-show-qr                    (rn/use-callback
                                       (fn []
                                         (rf/dispatch [:universal-links/generate-profile-url
                                                       {:public-key public-key
                                                        :cb         (#(rf/dispatch [:open-modal
                                                                                    :share-contact]))}]))
                                       [public-key])
        has-nickname?                 (rn/use-memo (fn [] (not (string/blank? nickname))) [nickname])]
    [quo/action-drawer
     [[{:icon                :i/edit
        :label               (if has-nickname?
                               (i18n/label :t/edit-nickname)
                               (i18n/label :t/add-nickname-title))
        :on-press            on-add-nickname
        :accessibility-label (if nickname :edit-nickname :add-nickname)}
       {:icon                :i/qr-code
        :label               (i18n/label :t/show-qr)
        :on-press            on-show-qr
        :accessibility-label :show-qr-code}
       {:icon                :i/share
        :label               (i18n/label :t/share-profile)
        :on-press            not-implemented/alert
        :accessibility-label :share-profile}
       (when has-nickname?
         {:icon                :i/delete
          :label               (i18n/label :t/remove-nickname)
          :on-press            on-remove-nickname
          :add-divider?        true
          :accessibility-label :remove-nickname
          :danger?             true})
       {:icon                :i/untrustworthy
        :label               (i18n/label :t/mark-untrustworthy)
        :on-press            not-implemented/alert
        :accessibility-label :mark-untrustworthy
        :add-divider?        (when-not has-nickname? true)
        :danger?             true}
       {:icon                :i/block
        :label               (i18n/label :t/block-user)
        :on-press            not-implemented/alert
        :accessibility-label :block-user
        :danger?             true}]]]))
