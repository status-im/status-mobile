(ns legacy.status-im.ui.screens.group.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.invite.views :as invite]
    [legacy.status-im.ui.components.keyboard-avoid-presentation :as kb-presentation]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.search-input.view :as search]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.group.styles :as styles]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn- render-contact
  [row]
  (let [{:keys [primary-name secondary-name]} row]
    [list.item/list-item
     {:title    primary-name
      :subtitle secondary-name
      :icon     [chat-icon/contact-icon-contacts-tab row]}]))

(defn- on-toggle-participant
  [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-participant public-key allow-new-users?])

    ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-participant public-key allow-new-users?])))

(defn- toggle-item
  []
  (fn [allow-new-users? subs-name {:keys [public-key] :as contact} on-toggle]
    (let [contact-selected?                     @(re-frame/subscribe [subs-name public-key])
          {:keys [primary-name secondary-name]} contact]
      [list.item/list-item
       {:title     primary-name
        :subtitle  secondary-name
        :icon      [chat-icon/contact-icon-contacts-tab contact]
        :on-press  #(on-toggle allow-new-users? contact-selected? public-key)
        :active    contact-selected?
        :accessory :checkbox}])))

(defn- group-toggle-participant
  [contact _ _ allow-new-users?]
  [toggle-item allow-new-users? :is-participant-selected? contact on-toggle-participant])

(defn toggle-list
  [{:keys [contacts render-fn render-data]}]
  [list/flat-list
   {:data                         contacts
    :key-fn                       :public-key
    :render-data                  render-data
    :render-fn                    render-fn
    :keyboard-should-persist-taps :always}])

(defn no-contacts-view
  [{:keys [no-contacts]}]
  [react/view {:style styles/no-contacts}
   [react/text
    {:style (styles/no-contact-text)}
    no-contacts]
   [invite/button]])

(defn filter-contacts
  [filter-text contacts]
  (let [lower-filter-text (string/lower-case (str filter-text))
        filter-fn         (fn [{:keys [name alias nickname]}]
                            (or
                             (string/includes? (string/lower-case (str name)) lower-filter-text)
                             (string/includes? (string/lower-case (str alias)) lower-filter-text)
                             (when nickname
                               (string/includes? (string/lower-case (str nickname))
                                                 lower-filter-text))))]
    (if filter-text
      (filter filter-fn contacts)
      contacts)))

;; Set name of new group-chat
(views/defview new-group
  []
  (views/letsubs [contacts   [:selected-group-contacts]
                  group-name [:new-chat-name]]
    (let [group-name-empty? (not (and (string? group-name) (not-empty group-name)))]
      [react/keyboard-avoiding-view
       {:style         styles/group-container
        :ignore-offset true}
       [react/view {:flex 1}
        [topbar/topbar
         {:use-insets false
          :title      (i18n/label :t/new-group-chat)
          :subtitle   (i18n/label :t/group-chat-members-count
                                  {:selected (inc (count contacts))
                                   :max      constants/max-group-chat-participants})}]
        [react/view
         {:style {:padding-top 16
                  :flex        1}}
         [react/view {:style {:padding-horizontal 16}}
          [quo/text-input
           {:auto-focus          true
            :on-change-text      #(re-frame/dispatch [:set :new-chat-name %])
            :default-value       group-name
            :placeholder         (i18n/label :t/set-a-topic)
            :accessibility-label :chat-name-input}]
          [react/text {:style (styles/members-title)}
           (i18n/label :t/members-title)]]
         [react/view
          {:style {:margin-top 8
                   :flex       1}}
          [list/flat-list
           {:data                         contacts
            :key-fn                       :address
            :render-fn                    render-contact
            :bounces                      false
            :keyboard-should-persist-taps :always
            :enable-empty-sections        true}]]]
        [toolbar/toolbar
         {:show-border? true
          :left
          [quo/button
           {:type                :secondary
            :before              :main-icon/back
            :accessibility-label :previous-button
            :on-press            #(re-frame/dispatch [:navigate-back])}
           (i18n/label :t/back)]
          :right
          [quo/button
           {:type                :secondary
            :accessibility-label :create-group-chat-button
            :disabled            group-name-empty?
            :on-press            #(debounce/dispatch-and-chill [:group-chats.ui/create-pressed
                                                                group-name]
                                                               300)}
           (i18n/label :t/create-group-chat)]}]]])))

(defn searchable-contact-list
  []
  (let [search-value (reagent/atom nil)]
    (fn [{:keys [contacts no-contacts-label toggle-fn allow-new-users?]}]
      [react/view {:style {:flex 1}}
       [react/view {:style (styles/search-container)}
        [search/search-input-old
         {:on-cancel #(reset! search-value nil)
          :on-change #(reset! search-value %)}]]
       [react/view
        {:style {:flex             1
                 :padding-vertical 8}}
        (if (seq contacts)
          [toggle-list
           {:contacts    (filter-contacts @search-value contacts)
            :render-data allow-new-users?
            :render-fn   toggle-fn}]
          [no-contacts-view {:no-contacts no-contacts-label}])]])))

;; Add participants to existing group chat
(views/defview add-participants-toggle-list
  []
  (views/letsubs [contacts                [:contacts/all-contacts-not-in-current-chat]
                  current-chat            [:chats/current-chat]
                  selected-contacts-count [:selected-participants-count]]
    (let [current-participants-count (count (:contacts current-chat))]
      [kb-presentation/keyboard-avoiding-view {:style styles/group-container}
       [topbar/topbar
        {:use-insets    false
         :border-bottom false
         :title         (i18n/label :t/add-members)
         :subtitle      (i18n/label :t/group-chat-members-count
                                    {:selected (+ current-participants-count selected-contacts-count)
                                     :max      constants/max-group-chat-participants})}]
       [searchable-contact-list
        {:contacts          contacts
         :no-contacts-label (i18n/label :t/group-chat-all-contacts-invited)
         :toggle-fn         group-toggle-participant
         :allow-new-users?  (< (+ current-participants-count
                                  selected-contacts-count)
                               constants/max-group-chat-participants)}]
       [toolbar/toolbar
        {:show-border? true
         :center
         [quo/button
          {:type                :secondary
           :accessibility-label :next-button
           :disabled            (zero? selected-contacts-count)
           :on-press            #(re-frame/dispatch [:group-chats.ui/add-members-pressed])}
          (i18n/label :t/add)]}]])))
