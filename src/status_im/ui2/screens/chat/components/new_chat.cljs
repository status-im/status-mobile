(ns status-im.ui2.screens.chat.components.new-chat
  (:require [quo2.components.buttons.button :as button]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as quo2.colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.group.styles :as styles]
            [utils.re-frame :as rf]
            [status-im.ui.screens.chat.sheets :refer [hide-sheet-and-dispatch]]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui2.screens.common.contact-list.view :as contact-list]))

(defn- on-toggle [allow-new-users? checked? public-key]
  (cond
    checked?
    (re-frame/dispatch [:deselect-contact public-key allow-new-users?])
    ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-contact public-key allow-new-users?])))

;; Start group chat
(defn contact-toggle-list []
  (let [contacts                   (rf/sub [:contacts/sorted-and-grouped-by-first-letter])
        selected-contacts-count    (rf/sub [:selected-contacts-count])
        window-height              (rf/sub [:dimensions/window-height])
        one-contact-selected?      (= selected-contacts-count 1)
        contacts-selected?         (pos? selected-contacts-count)
        {:keys [alias public-key]} (-> contacts first :data first)
        added                      (reagent/atom '())]
    [react/view  {:style {:height (* window-height 0.95)}}
     [topbar/topbar {:use-insets                 false
                     :border-bottom              false
                     :style                      {:top -15}
                     :close-icon-container-props {:style {:width            32
                                                          :height           32
                                                          :border-radius    10
                                                          :justify-content  :center
                                                          :align-items      :center
                                                          :background-color (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-80)}}
                     :close-icon-props           {:size  20
                                                  :color (quo2.colors/theme-colors quo2.colors/black quo2.colors/white)}
                     :navigation                 {:sheet? true}
                     :modal?                     true
                     :background                 (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)}]
     [react/view {:style {:flex-direction     :row
                          :justify-content    :space-between
                          :align-items        :flex-end
                          :padding-horizontal 20}}
      [quo2/text {:weight :semi-bold
                  :size   :heading-1
                  :color  (quo2.colors/theme-colors quo2.colors/neutral-40 quo2.colors/neutral-50)}
       (i18n/label :t/new-chat)]
      [quo2/text {:size            :paragraph-2
                  :weight          :regular
                  :secondary-color (quo2.colors/theme-colors quo2.colors/neutral-40 quo2.colors/neutral-50)}
       (i18n/label :t/selected-count-from-max
                   {:selected (inc selected-contacts-count)
                    :max      constants/max-group-chat-participants})]]
     [react/view {:style {:height 430}}
      [contact-list/contact-list
       {:icon              :check
        :group             nil
        :added             added
        :search?           false
        :start-a-new-chat? true
        :on-toggle         on-toggle}]]
     (when contacts-selected?
       [toolbar/toolbar
        {:show-border?  false
         :margin-bottom 20
         :center        [button/button {:type                :primary
                                        :accessibility-label :next-button
                                        :on-press            #(do
                                                                (if one-contact-selected?
                                                                  (hide-sheet-and-dispatch [:chat.ui/start-chat public-key])
                                                                  (hide-sheet-and-dispatch [:navigate-to :new-group])))}
                         (if one-contact-selected?
                           (i18n/label :t/chat-with {:selected-user alias})
                           (i18n/label :t/setup-group-chat))]}])]))
