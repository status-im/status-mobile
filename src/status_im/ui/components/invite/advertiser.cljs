(ns status-im.ui.components.invite.advertiser
  (:require [re-frame.core :as re-frame]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.ui.components.invite.events :as invite]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.invite.utils :as utils]
            [status-im.acquisition.advertiser :as advertiser]))

(defn perk [{name             :name
             {source :source} :icon} value]
  [rn/view {:style {:flex-direction   :row
                    :align-items      :center
                    :padding-vertical 4}}
   [rn/view {:style {:flex-direction :row}}
    [rn/image {:source (if (fn? source) (source) source)
               :style  {:width        20
                        :height       20
                        :margin-right 8}}]
    [quo/text {:size   :small
               :weight :medium}
     (str value " ")]
    [quo/text {:size   :small
               :weight :medium}
     name]]])

(defn token-icon-style [idx]
  {:align-items    :center
   :shadow-radius  16
   :shadow-opacity 1
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}
   :width          40
   :height         40
   :border-radius  20
   :left           (* idx -20)})

(defn accept-popover []
  (fn []
    (let [pack   @(re-frame/subscribe [::invite/starter-pack])
          tokens (utils/transform-tokens pack)]
      [rn/view {:style {:align-items        :center
                        :padding-vertical   16
                        :padding-horizontal 16}}
       [rn/view {:flex-direction :row
                 :height         40
                 :left           10}
        (for [[{name             :name
                {source :source} :icon} _ i] tokens]
          ^{:key name}
          [rn/view {:style (token-icon-style i)}
           [rn/image {:source (if (fn? source) (source) source)
                      :style  {:width  40
                               :height 40}}]])]
       [rn/view {:style {:padding 8}}
        [quo/text {:style {:margin-bottom 8}
                   :align :center
                   :size  :x-large}
         (i18n/label :t/advertiser-starter-pack-title)]
        [quo/text {:align :center}
         (i18n/label :t/advertiser-starter-pack-description)]]
       [rn/view {:style {:border-radius      8
                         :border-width       1
                         :border-color       (:ui-02 @colors/theme)
                         :width              "100%"
                         :margin-vertical    8
                         :padding-vertical   8
                         :padding-horizontal 12}}
        (for [[k v] tokens]
          ^{:key (:name k)}
          [perk k v])]
       [rn/view {:style {:margin-vertical 8}}
        [quo/button {:on-press #(re-frame/dispatch [::advertiser/decision :accept])}
         (i18n/label :t/advertiser-starter-pack-accept)]]
       [quo/button {:type     :secondary
                    :on-press #(re-frame/dispatch [::advertiser/decision :decline])}
        (i18n/label :t/advertiser-starter-pack-decline)]
       [rn/view {:padding-vertical 8}
        [quo/text {:color :secondary
                   :align :center
                   :size  :small}
         (i18n/label :t/invite-privacy-policy1)]
        [quo/text {:color    :link
                   :align    :center
                   :size     :small
                   :on-press #(re-frame/dispatch [::invite/terms-and-conditions])}
         (i18n/label :t/invite-privacy-policy2)]]])))
