(ns legacy.status-im.ui.components.invite.views
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
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
  [list.item/list-item
   {:theme               :accent
    :title               (i18n/label :t/invite-friends)
    :icon                :main-icons/share
    :accessibility-label accessibility-label
    :on-press            (fn []
                           (re-frame/dispatch [:bottom-sheet/hide-old])
                           (js/setTimeout
                            #(re-frame/dispatch [:invite.events/share-link nil])
                            250))}])




