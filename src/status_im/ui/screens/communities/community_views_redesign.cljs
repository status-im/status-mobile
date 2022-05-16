(ns status-im.ui.screens.communities.community-views-redesign
  (:require
   [quo2.components.text :as text]
   [status-im.i18n.i18n :as i18n]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt <sub]]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.community-redesign :as community-redesign]
   [status-im.ui.screens.communities.icon :as communities.icon]
   [status-im.ui.screens.communities.styles :as styles]
   [quo2.components.filter-tag :as quo2.filter-tag]
   [status-im.ui.components.icons.icons :as icons]
   [quo2.foundations.typography :as typography]
   [quo2.foundations.colors :as quo2.colors]
   [quo.design-system.colors :as quo.colors]))

(defn stats-column []
  [react/view {:flex-direction :row
               :align-items    :center}
   [react/view {:flex-direction :row
                :align-items    :center
                :margin-right   16}
    [icons/icon :main-icons2/group16 {:container-style {:opacity         0.4
                                                        :align-items     :center
                                                        :justify-content :center}
                                      :resize-mode      :center
                                      :color            (quo2.colors/theme-colors
                                                         quo2.colors/neutral-50
                                                         quo2.colors/neutral-40)}]
    [text/text {:style (merge typography/font-regular
                              typography/paragraph-1)}
     (i18n/format-members 630000)]]
   [react/view {:flex-direction :row
                :align-items    :center
                :margin-right 16}
    [icons/icon :main-icons2/lightning16 {:container-style {:opacity         0.4
                                                            :align-items     :center
                                                            :justify-content :center}
                                          :resize-mode      :center
                                          :color            (quo2.colors/theme-colors
                                                             quo2.colors/neutral-50
                                                             quo2.colors/neutral-40)}]
    [text/text {:style (merge typography/font-regular
                              typography/paragraph-1)}
     (i18n/format-members 3300)]]
   [react/view {:flex-direction :row
                :align-items    :center
                :margin-right   16}
    [icons/icon :main-icons2/placeholder16 {:container-style {:opacity         0.4
                                                              :align-items     :center
                                                              :justify-content :center}
                                            :resize-mode      :center
                                            :color            (quo2.colors/theme-colors
                                                               quo2.colors/neutral-50
                                                               quo2.colors/neutral-40)}]
    [text/text {:style (merge typography/font-regular
                              typography/paragraph-1)}
     (i18n/format-members 63)]]])

(defn community-tags [tags]
  [react/view {:flex-direction :row
               :margin-top     16}
   (for [{:keys [id label resource]} tags]
     ^{:key id}
     [react/view {:margin-right 8}
      [quo2.filter-tag/tag
       {:id       id
        :size     24
        :resource resource}
       label]])])

(defn community-card-list-item [{:keys [id name description members permissions-granted
                                        type tags section] :as community}]
  (let [permissions-bg-color (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-80)
        theme-color         (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
        window-width (* (<sub [:dimensions/window-width]) 0.90)]
    [react/view {:style (merge (styles/card-redesign window-width theme-color)
                               {:margin-bottom  12}
                               (if (= section "featured")
                                 {:margin-right       12
                                  :width              window-width}
                                 {:margin-bottom      16}))}
     [react/touchable-opacity {:style         (merge {:height               230
                                                      :border-radius        20})
                               :on-press      (fn []
                                                (>evt [::communities/load-category-states id])
                                                (>evt [:dismiss-keyboard])
                                                (>evt [:navigate-to :community {:community-id id}]))
                               :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [community-redesign/community-actions community])}])}
      [:<>
       [react/view {:height                  64
                    :border-top-right-radius 20
                    :border-top-left-radius  20
                    :background-color        quo2.colors/primary-50-opa-20}]
       [react/view {:flex               1
                    :margin-top         -24
                    :border-radius      16
                    :padding-horizontal 12
                    :background-color   theme-color}
        [react/view {:flex-direction    :row
                     :justify-content   :space-between}
         [react/view {:border-radius    32
                      :margin-top       -20
                      :padding          2
                      :background-color theme-color}
          [communities.icon/community-icon-redesign community 48]]
         (when (= type "token-gated")
           [react/view {:style {:flex-direction   :row
                                :border-radius    200
                                :width            48
                                :height           24
                                :align-items      :center
                                :margin-top       8
                                :padding          2
                                :background-color permissions-bg-color}}
            [icons/icon (if permissions-granted :main-icons2/unlocked16 :main-icons2/locked16)
             {:container-style {:opacity          0.4
                                :align-items      :center
                                :justify-content  :center}
              :resize-mode      :center
              :color            (:icon-02 @quo.colors/theme)}]
            [communities.icon/community-icon-redesign community 20]])]
        [text/text
         {:style (merge
                  typography/font-semi-bold
                  typography/heading-2)
          :accessibility-label :chat-name-text
          :number-of-lines     1
          :ellipsize-mode      :tail}
         name]
        [text/text
         {:style (merge typography/font-regular
                        typography/paragraph-1)
          :accessibility-label :community-description-text
          :number-of-lines     2
          :ellipsize-mode      :tail}
         description]

        [react/view {:flex-direction    :row
                     :margin-top        12
                     :align-items       :center}
         [stats-column members]]
        [community-tags tags]]]]]))

(defn categorized-communities-list-item [{:keys [id name members type] :as community}]
  (let [card-bg-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
        permissions-bg-color (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-80)
        permissions-granted  false
        window-width (* (<sub [:dimensions/window-width]) 0.90)]
    [react/view {:style (merge (styles/card-redesign window-width card-bg-color)
                               {:margin-bottom  12})}
     [react/touchable-opacity {:border-radius        20
                               :on-press      (fn []
                                                (>evt [::communities/load-category-states id])
                                                (>evt [:dismiss-keyboard])
                                                (>evt [:navigate-to :community {:community-id id}]))
                               :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [community-redesign/community-actions community])}])}
      [react/view {:flex               1}
       [react/view {:flex-direction    :row
                    :border-radius      16
                    :align-items       :center
                    :background-color   card-bg-color}
        [react/view {:border-radius    32
                     :padding          12
                     :background-color card-bg-color}
         [communities.icon/community-icon-redesign community 48]]
        [react/view
         [text/text
          {:style (merge
                   typography/font-semi-bold
                   typography/heading-2)
           :accessibility-label :chat-name-text
           :number-of-lines     1
           :ellipsize-mode      :tail}
          name]
         [react/view {:flex-direction    :row
                      :align-items       :center}
          [stats-column members]]]
        (when (= type "token-gated")
          [react/view {:flex             1
                       :align-items      :flex-end
                       :padding-right    12}
           [react/view {:flex-direction   :row
                        :border-radius    200
                        :width            48
                        :height           24
                        :align-items      :center
                        :margin-top       8
                        :padding          2
                        :background-color permissions-bg-color}
            [icons/icon (if permissions-granted :main-icons2/unlocked16 :main-icons2/locked16)
             {:container-style {:opacity          0.4
                                :align-items      :center
                                :justify-content  :center}
              :resize-mode      :center
              :color            (:icon-02 @quo.colors/theme)}]
            [communities.icon/community-icon-redesign community 20]]])]]]]))




