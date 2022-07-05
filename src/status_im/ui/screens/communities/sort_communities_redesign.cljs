(ns status-im.ui.screens.communities.sort-communities-redesign
  (:require [re-frame.core :as re-frame]
            [quo2.components.item :as list-item]
            [quo2.components.text :as text]
            [quo2.foundations.colors :as colors]
            [quo.theme :as theme]
            [status-im.ui.components.react :as react]
            [status-im.i18n.i18n :as i18n]))

(def icon-color (colors/theme-colors
                 colors/neutral-50
                 colors/neutral-40))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn sort-communities-view []
  [react/view
   [react/view {:margin-left      20
                :padding-bottom 12}
    [text/text
     {:style {:accessibility-label :sort-communities-title
              :color               icon-color
              :weight              :medium
              :size                :paragraph-2}}
     (i18n/label :t/sort-communities)]]
   [list-item/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/alphabetically)
     :accessibility-label :alphabetically
     :icon                :main-icons2/alphabetically
     :icon-color          icon-color}]
   [list-item/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/total-members)
     :accessibility-label :total-members
     :icon                :main-icons2/members
     :icon-color          icon-color}]
   [list-item/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/active-members)
     :accessibility-label :active-members
     :icon                :main-icons2/lightning
     :icon-color          icon-color}]
   [list-item/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/mutal-contacts)
     :accessibility-label :mutual-contacts
     :icon                :main-icons2/friend
     :icon-color          icon-color}]])

(def sort-communities
  {:content sort-communities-view})
