(ns status-im.ui2.screens.chat.components.new-chat.view
  (:require [quo2.components.buttons.button :as button]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as quo2.colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui2.screens.common.contact-list.view :as contact-list]
            [quo2.components.markdown.text :as text]
            [status-im.ui.components.invite.events :as invite.events]
            [status-im.ui2.screens.chat.components.new-chat.styles :as style]
            [status-im2.common.bottom-sheet.view :as bottom-sheet]))

(defn- hide-sheet-and-dispatch
  [evt-vec]
  (bottom-sheet/close-bottom-sheet-fn nil)
  (rf/dispatch evt-vec))

(defn- on-toggle
  [allow-new-users? checked? public-key]
  (cond
    checked?
    (re-frame/dispatch [:deselect-contact public-key allow-new-users?])
    ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-contact public-key allow-new-users?])))

(defn- no-contacts-view
  []
  [react/view
   {:style {:justify-content :center
            :align-items     :center
            :margin-top      120}}
   [quo2/icon :i/no-contacts
    {:size     120
     :no-color true}]
   [text/text
    {:weight :semi-bold
     :size   :paragraph-1
     :style  {:margin-bottom 2
              :margin-top    20}}
    "You have no contacts"]
   [text/text
    {:weight :regular
     :size   :label
     :style  {:margin-bottom 20}}
    "Invite your friends and family to Status"]
   [quo2/button
    {:type     :primary
     :style    {:margin-bottom 12}
     :on-press #(rf/dispatch [::invite.events/share-link nil])}
    "Invite friends"]
   [quo2/button
    {:type     :grey
     :on-press #(hide-sheet-and-dispatch [:open-modal :new-contact])}
    "Add a contact"]])

(defn contact-selection-list
  []
  (let [contacts                                     (rf/sub
                                                      [:contacts/sorted-and-grouped-by-first-letter])
        selected-contacts-count                      (rf/sub [:selected-contacts-count])
        one-contact-selected?                        (= selected-contacts-count 1)
        contacts-selected?                           (pos? selected-contacts-count)
        {:keys [names public-key]}                   (-> contacts first :data first)
        added?                                       (reagent/atom '())
        {:keys [nickname ens-name three-words-name]} names
        first-username                               (or ens-name nickname three-words-name)
        no-contacts?                                 (empty? contacts)]
    [react/view {:style {:flex 1}}
     [quo2/button
      {:type                      :grey
       :icon                      true
       :on-press                  #(bottom-sheet/close-bottom-sheet-fn nil)
       :style                     style/contact-selection-close
       :override-background-color (quo2.colors/theme-colors quo2.colors/neutral-10
                                                            quo2.colors/neutral-90)}
      :i/close]
     [react/view style/contact-selection-heading
      [quo2/text
       {:weight :semi-bold
        :size   :heading-1
        :style  {:color (quo2.colors/theme-colors quo2.colors/neutral-100 quo2.colors/white)}}
       (i18n/label :t/new-chat)]
      (when-not no-contacts?
        [quo2/text
         {:size   :paragraph-2
          :weight :regular
          :style  {:color (quo2.colors/theme-colors quo2.colors/neutral-40 quo2.colors/neutral-50)}}
         (i18n/label :t/selected-count-from-max
                     {:selected (inc selected-contacts-count)
                      :max      constants/max-group-chat-participants})])]
     [react/view
      {:style {:height        430
               :margin-bottom -20}}
      (if no-contacts?
        [no-contacts-view]
        [contact-list/contact-list
         {:icon              :check
          :group             nil
          :added?            added?
          :search?           false
          :start-a-new-chat? true
          :on-toggle         on-toggle}])]
     (when contacts-selected?
       [toolbar/toolbar
        {:show-border? false
         :center       [button/button
                        {:type                :primary
                         :accessibility-label :next-button
                         :on-press            #(do
                                                 (if one-contact-selected?
                                                   (hide-sheet-and-dispatch [:chat.ui/start-chat
                                                                             public-key])
                                                   (hide-sheet-and-dispatch [:navigate-to :new-group])))}
                        (if one-contact-selected?
                          (i18n/label :t/chat-with {:selected-user first-username})
                          (i18n/label :t/setup-group-chat))]}])]))
