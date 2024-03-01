(ns status-im.contexts.profile.contact.actions.view
  (:require [quo.core :as quo]
            [status-im.common.not-implemented :as not-implemented]
            [utils.i18n :as i18n]))

(defn view
  []
  [quo/action-drawer
   [[{:icon                :i/edit
      :label               (i18n/label :t/add-nickname-title)
      :on-press            not-implemented/alert
      :accessibility-label :add-nickname}
     {:icon                :i/qr-code
      :label               (i18n/label :t/show-qr)
      :on-press            not-implemented/alert
      :accessibility-label :show-qr-code}
     {:icon                :i/share
      :label               (i18n/label :t/share-profile)
      :on-press            not-implemented/alert
      :accessibility-label :share-profile}
     {:icon                :i/untrustworthy
      :label               (i18n/label :t/mark-untrustworthy)
      :on-press            not-implemented/alert
      :accessibility-label :mark-untrustworthy
      :add-divider?        true
      :danger?             true}
     {:icon                :i/block
      :label               (i18n/label :t/block-user)
      :on-press            not-implemented/alert
      :accessibility-label :block-user
      :danger?             true}]]])
