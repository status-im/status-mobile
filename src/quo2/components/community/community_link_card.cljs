(ns quo2.components.community.community-link-card
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.components.community.style :as style]
            [quo2.components.icon :as icons]
            [react-native.core :as rn]
            [status-im.i18n.i18n :as i18n]))

(defn community-title-and-description [title description image]
  [:<>
   [rn/view
    {:style {:padding-left    15
             :flex-direction  :row
             :align-items     :center}}
    [rn/image
     {:source image
      :style {:width  16
              :height 16}}]
    [rn/view
     {:style {:margin-left 5}}
     [text/text {:size         :paragraph-1
                 :weight       :semi-bold}
      title]]]
   [rn/view
    {:style {:padding-left 15}}
    [text/text {:size            :paragraph-2
                :weight          :regular}
     description]]])

(defn community-stats [{:keys [member-count active-members mutual-contacts]}]
  [rn/view
   {:style {:flex-direction    :row
            :align-self        :flex-start
            :margin-vertical   10
            :width             271
            :padding-left      15}}
   [rn/view
    {:style {:flex-direction  :row
             :align-items     :center}}
    [:<>
     [icons/icon  :i/group
      {:size    16
       :color   colors/neutral-50}]]
    [text/text {:size         :paragraph-2
                :weight       :regular}
     member-count]]
   [rn/view
    {:style {:flex-direction      :row
             :align-items         :center
             :padding-horizontal  10}}
    [:<>
     [icons/icon  :i/lightning
      {:size   16
       :color  colors/neutral-50
       :left   10}]]
    [text/text {:size         :paragraph-2
                :weight       :regular}
     active-members]]
   [rn/view
    {:style {:flex-direction  :row
             :align-items     :center}}
    [:<>
     [icons/icon :i/placeholder
      {:size   16
       :color  colors/neutral-50}]]
    [text/text {:size         :paragraph-2
                :weight       :regular}
     mutual-contacts]]])

(defn community-link-card [{:keys [title description image member-count active-members mutual-contacts]}]
  [rn/view (merge (style/community-card 16)
                  {:background-color  (colors/theme-colors
                                       colors/white
                                       colors/neutral-90)}
                  {:height           176
                   :width            350
                   :align-self       :center
                   :padding-vertical 12
                   :margin-vertical  20})
   [community-title-and-description title description image]
   [community-stats {:member-count    member-count
                     :active-members  active-members
                     :mutual-contacts mutual-contacts}]
   [rn/touchable-opacity
    {:style {:height             40
             :width              320
             :flex-direction     :row
             :align-self         :center
             :align-items        :center
             :justify-content    :center
             :background-color   colors/community-link-card-button
             :border-radius      10
             :padding-vertical   5
             :padding-horizontal 12}}
    [:<>
     [icons/icon :i/communities
      {:size  20
       :color colors/white}]]
    [text/text {:style {:size           :paragraph-1
                        :weight         :font-medium
                        :padding-left   10
                        :color          colors/white}}
     (i18n/label :t/community-link-card-button-text)]]])

