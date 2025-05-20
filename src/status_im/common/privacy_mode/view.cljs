(ns status-im.common.privacy-mode.view
  (:require [quo.core :as quo]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.not-implemented :as not-implemented]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [privacy-mode-enabled?]
  [:<>
   [quo/drawer-top
    {:title (i18n/label :t/privacy-mode)}]
   [quo/text {:style {:margin-vertical 4 :margin-horizontal 20}}
    (i18n/label :t/privacy-mode-message-1)]
   [quo/text
    {:style  {:margin-horizontal 20 :margin-top 16 :margin-bottom 4}
     :weight :semi-bold
     :size   :paragraph-1}
    (i18n/label :t/feature-that-will-be-unavailable)]
   [quo/status-list-item
    {:status :negative :title (i18n/label :t/feature-that-will-be-unavailable-item-1)}]
   [quo/status-list-item
    {:status :negative :title (i18n/label :t/feature-that-will-be-unavailable-item-2)}]
   [quo/status-list-item
    {:status :negative :title (i18n/label :t/feature-that-will-be-unavailable-item-3)}]
   [quo/status-list-item
    {:status :negative :title (i18n/label :t/feature-that-will-be-unavailable-item-4)}]
   [quo/status-list-item
    {:status :negative :title (i18n/label :t/feature-that-will-be-unavailable-item-5)}]
   [quo/text
    {:style  {:margin-horizontal 20 :margin-top 16 :margin-bottom 4}
     :weight :semi-bold
     :size   :paragraph-1}
    (i18n/label :t/you-may-also-experience)]
   [quo/status-list-item {:status :negative :title (i18n/label :t/you-may-also-experience-item-1)}]
   [quo/status-list-item {:status :negative :title (i18n/label :t/you-may-also-experience-item-2)}]
   [quo/text {:style {:margin-top 16 :margin-bottom 8 :margin-horizontal 20}}
    (i18n/label :t/privacy-mode-message-2)]
   [quo/button
    {:type            :outline
     :size            24
     :icon-right      :i/external
     :container-style {:margin-top 8 :margin-bottom 12 :align-self :flex-start :margin-horizontal 20}
     :on-press        not-implemented/alert}
    (i18n/label :t/privacy-mode-more-details)]
   [quo/bottom-actions
    {:actions          :two-actions
     :blur?            true
     :button-one-label (i18n/label :t/close)
     :button-one-props {:on-press events-helper/hide-bottom-sheet
                        :type     :grey}
     :button-two-label (if privacy-mode-enabled?
                         (i18n/label :t/disable-privacy-mode)
                         (i18n/label :t/enable-privacy-mode))
     :button-two-props {:on-press            #(rf/dispatch [:privacy-mode/toggle-privacy-mode
                                                            privacy-mode-enabled?])
                        :customization-color :purple}}]])
