(ns status-im.ui.components.invite.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]))

(defn button
  []
  [quo/button
   {:on-press            #(re-frame/dispatch [:invite.events/share-link nil])
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
                           (re-frame/dispatch [:bottom-sheet/hide-old])
                           (js/setTimeout
                            #(re-frame/dispatch [:invite.events/share-link nil])
                            250))}])




