(ns status-im.ui.screens.communities.sort-communities-redesign
  (:require [re-frame.core :as re-frame]
            [quo2.components.item-redesign :as list-item]
            [quo2.components.text :as text]
            [quo2.foundations.typography :as typography]
            [quo2.foundations.colors :as colors]
            [quo.theme :as theme]
            [status-im.ui.components.react :as react]
            [status-im.i18n.i18n :as i18n]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn sort-communities-view []
  [react/view
   [react/view {:margin-left      20
                :padding-bottom 12}
    [text/text
     {:style (merge {:accessibility-label :sort-communities-title
                     :color            (colors/theme-colors
                                        colors/neutral-50
                                        colors/neutral-40)}
                    typography/paragraph-2
                    typography/font-semi-bold)}
     (i18n/label :t/sort-communities)]]
   [list-item/list-item
    {:theme               :dark
     :title               (i18n/label :t/alphabetically)
     :accessibility-label :alphabetically
     :icon                :main-icons2/alphabetically
     :on-press            #(hide-sheet-and-dispatch
                            [:communities.core/sort-communities-list :alphabetically])}]
   [list-item/list-item
    {:theme               :dark
     :title               (i18n/label :t/total-members)
     :accessibility-label :total-members
     :icon                :main-icons2/members
     :on-press            #(hide-sheet-and-dispatch
                            [:communities.core/sort-communities-list :total-members])}]
   [list-item/list-item
    {:theme               :dark
     :title               (i18n/label :t/active-members)
     :accessibility-label :active-members
     :icon                :main-icons2/lightning
     :on-press            #(hide-sheet-and-dispatch
                            [:communities.core/sort-communities-list :active-members])}]
   [list-item/list-item
    {:theme               :dark
     :title               (i18n/label :t/mutal-contacts)
     :accessibility-label :mutual-contacts
     :icon                :main-icons2/friend
     :on-press            #(hide-sheet-and-dispatch
                            [:communities.core/sort-communities-list :mutual-contacts])}]])

(def sort-communities
  {:content sort-communities-view})
