(ns status-im.ui.components.invite.modal
  (:require [re-frame.core :as re-frame]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.invite.style :as styles]
            [status-im.ui.components.invite.events :as invite]
            [status-im.acquisition.gateway :as gateway]
            [status-im.ui.components.invite.utils :as utils]))

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

(defn popover [{:keys [on-accept has-reward on-decline accept-label title description]}]
  (fn []
    (let [pack    (when has-reward @(re-frame/subscribe [::invite/starter-pack]))
          loading (#{(get gateway/network-statuses :initiated)
                     (get gateway/network-statuses :in-flight)}
                   @(re-frame/subscribe [::gateway/network-status]))
          tokens  (when has-reward (utils/transform-tokens pack))]
      [rn/view {:style {:align-items        :center
                        :padding-vertical   16
                        :padding-horizontal 16}}
       (when has-reward
         [rn/view {:style (styles/modal-tokens-icons-style (count tokens))}
          (for [[{name             :name
                  {source :source} :icon} _ i] tokens]
            ^{:key name}
            [rn/view {:style (styles/modal-token-icon-style i)}
             [rn/image {:source (if (fn? source) (source) source)
                        :style  {:width  40
                                 :height 40}}]])])
       [rn/view {:style {:padding 8}}
        (when title
          [quo/text {:style {:margin-bottom 8}
                     :align :center
                     :size  :x-large}
           title])
        [quo/text {:align :center}
         description]]
       (when has-reward
         [rn/view {:style (styles/modal-perks-container)}
          (for [[k v] tokens]
            ^{:key (:name k)}
            [perk k v])])
       [rn/view {:style {:margin-vertical 8}}
        [quo/button {:on-press on-accept
                     :loading  loading}
         accept-label]]
       [rn/view {:style {:opacity (if loading 0 1)}}
        [quo/button {:type     :secondary
                     :on-press on-decline}
         (i18n/label :t/advertiser-starter-pack-decline)]]
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
