(ns status-im.ui2.screens.quo2-preview.community.community-link-card
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [status-im.react-native.resources :as resources]
            [quo2.components.community.community-link-card :as community-link-card]))

(defn cool-preview []
    [rn/view
     [community-link-card/community-link-card {:title              (i18n/label :t/decent-title)
                                               :description        (i18n/label :t/decent-desc)
                                               :image              (resources/get-image :decentraland)
                                               :member_count       (i18n/label :t/member-count)
                                               :active-members     (i18n/label :t/active-members)
                                               :mutual-contacts    (i18n/label :t/mutual-contacts)}]])

(defn preview-community-card []
      [rn/view {:background-color (colors/theme-colors colors/neutral-5
                                                       colors/neutral-95)
                :flex             1}
       [rn/flat-list {:flex                      1
                      :keyboardShouldPersistTaps :always
                      :header                    [cool-preview]
                      :key-fn                    str}]])