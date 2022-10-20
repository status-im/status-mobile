(ns status-im.ui.screens.communities.sort-communities
  (:require [re-frame.core :as re-frame]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo.core :as quo]
            [quo.theme :as theme]
            [status-im.ui.components.react :as react]
            [status-im.i18n.i18n :as i18n]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn sort-communities-view []
  [:<>
   [react/view {:margin-left    20
                :padding-bottom 12}
    [text/text
     {:style {:accessibility-label :sort-communities-title
              :color               (colors/theme-colors
                                    colors/neutral-50
                                    colors/neutral-40)
              :weight              :medium
              :size                :paragraph-2}}
     (i18n/label :t/sort-communities)]]
   [quo/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/alphabetically)
     :accessibility-label :alphabetically
     :icon                :main-icons2/alphabetically
     :new-ui?             true}]
   [quo/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/total-members)
     :accessibility-label :total-members
     :icon                :main-icons2/members
     :new-ui?             true}]
   [quo/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/active-members)
     :accessibility-label :active-members
     :icon                :main-icons2/flash
     :new-ui?             true}]
   [quo/list-item
    {:theme               (theme/get-theme)
     :title               (i18n/label :t/mutal-contacts)
     :accessibility-label :mutual-contacts
     :icon                :main-icons2/friend
     :new-ui?             true}]])

(def sort-communities
  {:content sort-communities-view})
