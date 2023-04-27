(ns status-im2.contexts.communities.menus.leave.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [status-im2.contexts.communities.menus.generic-menu.view :as generic-menu]
            [status-im2.contexts.communities.menus.leave.style :as style]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch event))

(defn leave-sheet
  [id]
  [generic-menu/view
   {:id    id
    :title (i18n/label :t/leave-community?)}
   [:<>
    [quo/text
     {:accessibility-label :communities-join-community
      :size                :paragraph-1
      :style               style/text}
     (i18n/label :t/leave-community-message)]
    [quo/text
     {:size  :paragraph-1
      :style style/text}
     (i18n/label :t/leave-community-note)]
    [rn/view
     {:style style/button-container}
     [quo/button
      {:on-press #(rf/dispatch [:hide-bottom-sheet])
       :type     :grey
       :style    style/cancel-button}
      (i18n/label :t/cancel)]
     [quo/button
      {:on-press #(hide-sheet-and-dispatch [:communities/request-to-leave id])
       :style    style/action-button}
      (i18n/label :t/leave-community)]]]])

(defn cancel-request-sheet
  [id request-id]
  [generic-menu/view
   {:id    id
    :title (i18n/label :t/cancel-request?)}
   [:<>
    [quo/text
     {:accessibility-label :communities-join-community
      :size                :paragraph-1
      :style               style/text}
     (i18n/label :t/if-you-cancel)]
    [rn/view
     {:style style/button-container}
     [quo/button
      {:accessibility-label :cancel-button
       :on-press            #(rf/dispatch [:hide-bottom-sheet])
       :type                :grey
       :style               style/cancel-button}
      (i18n/label :t/close)]
     [quo/button
      {:accessibility-label :confirm-button
       :on-press            #(hide-sheet-and-dispatch [:communities/cancel-request-to-join request-id])
       :style               style/action-button}
      (i18n/label :t/confirm)]]]])
