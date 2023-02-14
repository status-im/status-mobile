(ns status-im.ui2.screens.chat.components.new-chat.view
  (:require [quo2.components.buttons.button :as button]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as quo2.colors]
            [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.common.contact-list.view :as contact-list]
            [quo2.components.markdown.text :as text]
            [status-im.ui.components.invite.events :as invite.events]
            [status-im.ui2.screens.chat.components.new-chat.styles :as style]
            [status-im.react-native.resources :as resources]
            [status-im2.common.contact-list-item.view :as contact-list-item]))

(defn- hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn- no-contacts-view
  []
  [rn/view
   {:style {:justify-content :center
            :align-items     :center
            :margin-top      120}}
   [rn/image
    {:source (resources/get-image (if (quo2.colors/dark?) :no-contacts-dark :no-contacts))}]
   [text/text
    {:weight :semi-bold
     :size   :paragraph-1
     :style  {:margin-bottom 2
              :margin-top    20}}
    (i18n/label :t/you-have-no-contacts)]
   [text/text
    {:weight :regular
     :size   :label
     :style  {:margin-bottom 20}}
    (i18n/label :t/invite-friends-and-family)]
   [quo2/button
    {:type     :primary
     :style    {:margin-bottom 12}
     :on-press #(rf/dispatch [::invite.events/share-link nil])}
    (i18n/label :t/invite-friends)]
   [quo2/button
    {:type     :grey
     :on-press #(hide-sheet-and-dispatch [:open-modal :new-contact])}
    (i18n/label :t/add-a-contact)]])

(defn contact-item-render
  [_]
  (fn [{:keys [public-key] :as item}]
    (let [user-selected? (rf/sub [:is-contact-selected? public-key])
          on-toggle      #(if user-selected?
                            (re-frame/dispatch [:deselect-contact public-key])
                            (re-frame/dispatch [:select-contact public-key]))]
      [contact-list-item/contact-list-item
       {:on-press  on-toggle
        :accessory {:type     :checkbox
                    :checked? user-selected?
                    :on-check on-toggle}}
       item])))

(defn contact-selection-list
  []
  [:f>
   (fn []
     (let [contacts                          (rf/sub [:contacts/sorted-and-grouped-by-first-letter])
           selected-contacts-count           (rf/sub [:selected-contacts-count])
           selected-contacts                 (rf/sub [:group/selected-contacts])
           window-height                     (rf/sub [:dimensions/window-height])
           one-contact-selected?             (= selected-contacts-count 1)
           contacts-selected?                (pos? selected-contacts-count)
           {:keys [primary-name public-key]} (when one-contact-selected?
                                               (rf/sub [:contacts/contact-by-identity
                                                        (first selected-contacts)]))
           no-contacts?                      (empty? contacts)]
       [rn/view {:style {:height (* window-height 0.9)}}
        [quo2/button
         {:type                      :grey
          :icon                      true
          :on-press                  #(rf/dispatch [:bottom-sheet/hide])
          :style                     style/contact-selection-close
          :override-background-color (quo2.colors/theme-colors quo2.colors/neutral-10
                                                               quo2.colors/neutral-90)}
         :i/close]
        [rn/view style/contact-selection-heading
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
                        {:selected selected-contacts-count
                         :max      constants/max-group-chat-participants})])]
        [rn/view
         {:style {:flex 1}}
         (if no-contacts?
           [no-contacts-view]
           [rn/section-list
            {:key-fn                         :title
             :sticky-section-headers-enabled false
             :sections                       (rf/sub [:contacts/filtered-active-sections])
             :render-section-header-fn       contact-list/contacts-section-header
             :content-container-style        {:padding-bottom 70}
             :render-fn                      contact-item-render}])]
        (when contacts-selected?
          [button/button
           {:type                :primary
            :style               style/chat-button
            :accessibility-label :next-button
            :on-press            (fn []
                                   (if one-contact-selected?
                                     (hide-sheet-and-dispatch [:chat.ui/start-chat
                                                               public-key])
                                     (hide-sheet-and-dispatch [:navigate-to
                                                               :new-group])))}
           (if one-contact-selected?
             (i18n/label :t/chat-with {:selected-user primary-name})
             (i18n/label :t/setup-group-chat))])]))])
