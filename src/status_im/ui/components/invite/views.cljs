(ns status-im.ui.components.invite.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.ui.components.invite.events :as invite.events]))

(defn button
  []
  [quo/button
   {:on-press            #(re-frame/dispatch [::invite.events/share-link nil])
    :accessibility-label :invite-friends-button}
   (i18n/label :t/invite-friends)])

(defn list-item
  [{:keys [accessibility-label]}]
  [quo/list-item
   {:theme               :accent
    :title               (i18n/label :t/invite-friends)
    :icon                :main-icons/share
    :accessibility-label accessibility-label
    :on-press            (fn []
                           (re-frame/dispatch [:bottom-sheet/hide])
                           (js/setTimeout
                            #(re-frame/dispatch [::invite.events/share-link nil])
                            250))}])




