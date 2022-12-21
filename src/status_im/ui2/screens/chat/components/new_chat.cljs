(ns status-im.ui2.screens.chat.components.new-chat
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [quo.core :as quo]
            [quo2.components.buttons.button :as button]
            [quo2.core :as quo2]
            [quo2.foundations.colors :as quo2.colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.group.styles :as styles]
            [utils.re-frame :as rf]
            [utils.debounce :as debounce]
            [status-im.ui.screens.chat.sheets :refer [hide-sheet-and-dispatch]]
            [status-im.ui.components.search-input.view :as search]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui2.screens.common.contact-list.view :as contact-list]))

(defn- render-contact [row]
  (let [[first-name second-name] (multiaccounts/contact-two-names row false)]
    [quo2/list-item
     {:title    first-name
      :subtitle second-name
      :icon     [chat-icon/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo row)]}]))

(defn- on-toggle [allow-new-users? checked? public-key]
  (cond
    checked?
    (re-frame/dispatch [:deselect-contact public-key allow-new-users?])
    ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-contact public-key allow-new-users?])))

(defn- on-toggle-participant [allow-new-users? checked? public-key]
  (cond
    checked?
    (re-frame/dispatch [:deselect-participant public-key allow-new-users?])

   ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-participant public-key allow-new-users?])))

(defn- toggle-item []
  (fn [allow-new-users? subs-name {:keys [public-key] :as contact} on-toggle]
    (let [contact-selected?        @(re-frame/subscribe [subs-name public-key])
          [first-name second-name] (multiaccounts/contact-two-names contact true)]
      [quo2/list-item
       {:title            first-name
        :text-color       (quo2.colors/theme-colors quo2.colors/neutral-100 quo2.colors/white)
        :subtitle         second-name
        :icon             [chat-icon/contact-icon-contacts-tab
                           (multiaccounts/displayed-photo contact)]
        :on-press         #(on-toggle allow-new-users? contact-selected? public-key)
        :active           contact-selected?
        :accessory        :checkbox
        :background-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)}])))

(defn- group-toggle-contact [{:keys [:allow-new-users?] :as contact} _ _]
  [toggle-item allow-new-users? :is-contact-selected? contact on-toggle])

(defn- group-toggle-participant [{:keys [:allow-new-users?] :as contact} _ _]
  [toggle-item allow-new-users? :is-participant-selected? contact on-toggle-participant])

(defn toggle-list [{:keys [contacts render-fn]}]
  [list/section-list
   {:content-container-style        {:padding-vertical 8}
    :key-fn                         :id
    :keyboard-should-persist-taps   :always
    :sticky-section-headers-enabled false
    :sections                       contacts
    :render-section-header-fn       quo2/index
    :render-fn                      render-fn}])

(defn no-contacts [{:keys [no-contacts]}]
  [react/view {:style styles/no-contacts}
   [react/text
    {:style (styles/no-contact-text)}
    no-contacts]
   [invite/button]])

(defn filter-contacts [filter-text contacts]
  (let [lower-filter-text (string/lower-case (str filter-text))
        filter-fn         (fn [{:keys [data]}]
                            (let [{:keys [name alias nickname]} (first data)]
                              (or
                               (string/includes? (string/lower-case (str name)) lower-filter-text)
                               (string/includes? (string/lower-case (str alias)) lower-filter-text)
                               (when nickname
                                 (string/includes? (string/lower-case (str nickname)) lower-filter-text)))))]
    (if filter-text
      (filter filter-fn contacts)
      contacts)))

;; Set name of new group-chat
(defn new-group []
  (let [contacts          (rf/sub [:selected-group-contacts])
        group-name        (rf/sub [:new-chat-name])
        group-name-empty? (not (spec/valid? :global/not-empty-string group-name))]
    [react/keyboard-avoiding-view  {:style         styles/group-container
                                    :ignore-offset true}
     [react/view {:flex 1}
      [topbar/topbar {:use-insets false
                      :title      (i18n/label :t/new-group-chat)
                      :subtitle   (i18n/label :t/group-chat-members-count
                                              {:selected (inc (count contacts))
                                               :max      constants/max-group-chat-participants})}]
      [react/view {:style {:padding-top 16
                           :flex        1}}
       [react/view {:style {:padding-horizontal 16}}
        [quo/text-input
         {:auto-focus          true
          :on-change-text      #(rf/dispatch [:set :new-chat-name %])
          :default-value       group-name
          :placeholder         (i18n/label :t/set-a-topic)
          :accessibility-label :chat-name-input}]
        [react/text {:style (styles/members-title)}
         (i18n/label :t/members-title)]]
       [react/view {:style {:margin-top 8
                            :flex       1}}
        [list/flat-list {:data                         contacts
                         :key-fn                       :address
                         :render-fn                    render-contact
                         :bounces                      false
                         :keyboard-should-persist-taps :always
                         :enable-empty-sections        true}]]]
      [toolbar/toolbar
       {:show-border? true
        :left         [quo/button {:type                :secondary
                                   :before              :main-icon/back
                                   :accessibility-label :previous-button
                                   :on-press            #(rf/dispatch [:navigate-back])}
                       (i18n/label :t/back)]
        :right        [quo/button {:type                :secondary
                                   :accessibility-label :create-group-chat-button
                                   :disabled            group-name-empty?
                                   :on-press            #(debounce/dispatch-and-chill [:group-chats.ui/create-pressed group-name]
                                                                                      300)}
                       (i18n/label :t/create-group-chat)]}]]]))

;; Start group chat
(defn contact-toggle-list []
  (let [contacts                   (rf/sub [:contacts/sorted-and-grouped-by-first-letter])
        selected-contacts-count    (rf/sub [:selected-contacts-count])
        window-height              (rf/sub [:dimensions/window-height])
        one-contact-selected?      (= selected-contacts-count 1)
        no-contacts-selected?      (zero? selected-contacts-count)
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
       {:icon    :check
        :group   nil
        :added   added
        :search? false
        :start-a-new-chat? true
        :on-toggle on-toggle}]]
     (when-not no-contacts-selected?
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
