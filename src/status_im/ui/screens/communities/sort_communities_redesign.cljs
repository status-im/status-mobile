(ns status-im.ui.screens.communities.sort-communities-redesign
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [quo2.components.text :as text]
            [quo2.foundations.typography :as typography]
            [quo2.foundations.colors :as colors]
            [status-im.i18n.i18n :as i18n]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn sort-communities-view []
  [react/view
   [text/text
    {:style (merge {:accessibility-label :sort-communities-title
                    :marging-left     20
                    :color            (colors/theme-colors
                                       colors/neutral-50
                                       colors/neutral-40)}
                   typography/paragraph-2
                   typography/font-semi-bold)}
    (i18n/label :t/sort-communities)]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/alphabetically)
     :accessibility-label :alphabetically
     :icon                :main-icons2/alphabetically}
    :on-press            #(hide-sheet-and-dispatch [:navigate-to :communities])]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/total-members)
     :accessibility-label :total-members
     :icon                :main-icons2/members}]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/active-members)
     :accessibility-label :active-members
     :icon                :main-icons2/lightning}]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/mutal-contacts)
     :accessibility-label :mutual-contacts
     :icon                :main-icons2/friend}]])

(def sort-communities
  {:content sort-communities-view})
