(ns status-im.ui.screens.communities.views (:require-macros [status-im.utils.views :as views])
    (:require
     [reagent.core :as reagent]
     [re-frame.core :as re-frame]
     [quo.core :as quo]
     [status-im.i18n.i18n :as i18n]
     [status-im.utils.core :as utils]
     [status-im.utils.config :as config]
     [status-im.constants :as constants]
     [status-im.communities.core :as communities]
     [status-im.ui.screens.home.views.inner-item :as inner-item]
     [status-im.ui.screens.home.styles :as home.styles]
     [status-im.ui.components.list.views :as list]
     [status-im.ui.components.copyable-text :as copyable-text]
     [status-im.react-native.resources :as resources]
     [status-im.ui.components.topbar :as topbar]
     [status-im.ui.components.icons.icons :as icons]
     [status-im.ui.components.colors :as colors]
     [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
     [status-im.ui.components.toolbar :as toolbar]
     [status-im.ui.components.react :as react]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn community-list-item [{:keys [id description]}]
  (let [identity (:identity description)]
    [quo/list-item
     {:icon                       (if (= id constants/status-community-id)
                                    [react/image {:source (resources/get-image :status-logo)
                                                  :style {:width 40
                                                          :height 40}}]

                                    [chat-icon.screen/chat-icon-view-chat-list
                                     id
                                     true
                                     (:display-name identity)
                                     ;; TODO: should be derived by id
                                     (or (:color identity)
                                         (rand-nth colors/chat-colors))
                                     false
                                     false])
      :title                     [react/view {:flex-direction :row
                                              :flex           1}
                                  [react/view {:flex-direction :row
                                               :flex           1
                                               :padding-right  16
                                               :align-items    :center}
                                   [quo/text {:weight              :medium
                                              :accessibility-label :community-name-text
                                              :ellipsize-mode      :tail
                                              :number-of-lines     1}
                                    (utils/truncate-str (:display-name identity) 30)]]]
      :title-accessibility-label :community-name-text
      :subtitle                  [react/view {:flex-direction :row}
                                  [react/view {:flex 1}
                                   [quo/text
                                    (utils/truncate-str (:description identity) 30)]]]
      :on-press                  #(do
                                    (re-frame/dispatch [:dismiss-keyboard])
                                    (re-frame/dispatch [:navigate-to :community id]))}]))

(defn communities-actions []
  [react/view
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/import-community)
     :accessibility-label :community-import-community
     :icon                :main-icons/check
     :on-press            #(hide-sheet-and-dispatch [::communities/import-pressed])}]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/create-community)
     :accessibility-label :community-create-community
     :icon                :main-icons/check
     :on-press            #(hide-sheet-and-dispatch [::communities/create-pressed])}]])

(views/defview communities []
  (views/letsubs [communities [:communities]]
    [react/view {:flex 1}
     [topbar/topbar (cond-> {:title (i18n/label :t/communities)}
                      config/communities-management-enabled?
                      (assoc :right-accessories [{:icon                :main-icons/more
                                                  :accessibility-label :chat-menu-button
                                                  :on-press
                                                  #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                       {:content (fn []
                                                                                   [communities-actions])
                                                                        :height  256}])}]))]
     [react/scroll-view {:style                   {:flex 1}
                         :content-container-style {:padding-vertical 8}}
      [list/flat-list
       {:key-fn                       :id
        :keyboard-should-persist-taps :always
        :data                         (vals communities)
        :render-fn                    (fn [community] [community-list-item community])}]]
     (when config/communities-management-enabled?
       [toolbar/toolbar
        {:show-border? true
         :center [quo/button {:on-press #(re-frame/dispatch [::communities/create-pressed])}
                  (i18n/label :t/create)]}])]))

(defn valid? [community-name community-description]
  (and (not= "" community-name)
       (not= "" community-description)))

(defn import-community []
  (let [community-key (reagent/atom "")]
    (fn []
      [react/view {:style {:padding-left    16
                           :padding-right   8}}
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label          (i18n/label :t/community-key)
          :placeholder    (i18n/label :t/community-key-placeholder)
          :on-change-text #(reset! community-key %)
          :auto-focus     true}]]
       [react/view {:style {:padding-top 20
                            :padding-horizontal 20}}
        [quo/button {:disabled  (= @community-key "")
                     :on-press #(re-frame/dispatch [::communities/import-confirmation-pressed @community-key])}
         (i18n/label :t/import)]]])))

(defn create []
  (let [community-name (reagent/atom "")
        membership  (reagent/atom 1)
        community-description (reagent/atom "")]
    (fn []
      [react/view {:style {:padding-left    16
                           :padding-right   8}}
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label          (i18n/label :t/name-your-community)
          :placeholder    (i18n/label :t/name-your-community-placeholder)
          :on-change-text #(reset! community-name %)
          :auto-focus     true}]]
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label           (i18n/label :t/give-a-short-description-community)
          :placeholder     (i18n/label :t/give-a-short-description-community)
          :multiline       true
          :number-of-lines 4
          :on-change-text  #(reset! community-description %)}]]
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label           (i18n/label :t/membership-type)
          :placeholder     (i18n/label :t/membership-type-placeholder)
          :on-change-text  #(reset! membership %)}]]

       [react/view {:style {:padding-top 20
                            :padding-horizontal 20}}
        [quo/button {:disabled  (not (valid? @community-name @community-description))
                     :on-press #(re-frame/dispatch [::communities/create-confirmation-pressed @community-name @community-description @membership])}
         (i18n/label :t/create)]]])))

(def create-sheet
  {:content create})

(def import-sheet
  {:content import-community})

(defn create-channel []
  (let [channel-name (reagent/atom "")
        channel-description (reagent/atom "")]
    (fn []
      [react/view {:style {:padding-left    16
                           :padding-right   8}}
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label          (i18n/label :t/name-your-channel)
          :placeholder    (i18n/label :t/name-your-channel-placeholder)
          :on-change-text #(reset! channel-name %)
          :auto-focus     true}]]
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label           (i18n/label :t/give-a-short-description-channel)
          :placeholder     (i18n/label :t/give-a-short-description-channel)
          :multiline       true
          :number-of-lines 4
          :on-change-text  #(reset! channel-description %)}]]

       (when config/communities-management-enabled?
         [react/view {:style {:padding-top 20
                              :padding-horizontal 20}}
          [quo/button {:disabled  (not (valid? @channel-name @channel-description))
                       :on-press #(re-frame/dispatch [::communities/create-channel-confirmation-pressed @channel-name @channel-description])}
           (i18n/label :t/create)]])])))

(def create-channel-sheet
  {:content create-channel})

(defn invite-people []
  (let [user-pk (reagent/atom "")]
    (fn []
      [react/view {:style {:padding-left    16
                           :padding-right   8}}
       [react/view {:style {:padding-horizontal 20}}
        [quo/text-input
         {:label          (i18n/label :t/enter-user-pk)
          :placeholder    (i18n/label :t/enter-user-pk)
          :on-change-text #(reset! user-pk %)
          :auto-focus     true}]]
       [react/view {:style {:padding-top 20
                            :padding-horizontal 20}}
        [quo/button {:disabled  (= "" user-pk)
                     :on-press #(re-frame/dispatch [::communities/invite-people-confirmation-pressed @user-pk])}
         (i18n/label :t/invite)]]])))

(def invite-people-sheet
  {:content invite-people})

(defn community-actions [id admin]
  [react/view
   (when (and config/communities-management-enabled? admin)
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/export-key)
       :accessibility-label :community-export-key
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [::communities/export-pressed id])}])
   (when (and config/communities-management-enabled? admin)
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/create-channel)
       :accessibility-label :community-create-channel
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [::communities/create-channel-pressed id])}])
   (when (and config/communities-management-enabled? admin)
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/invite-people)
       :accessibility-label :community-invite-people
       :icon                :main-icons/close
       :on-press            #(re-frame/dispatch [::communities/invite-people-pressed id])}])
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/leave)
     :accessibility-label :leave
     :icon                :main-icons/close
     :on-press            #(do
                             (re-frame/dispatch [:navigate-to :home])
                             (re-frame/dispatch [:bottom-sheet/hide])
                             (re-frame/dispatch [::communities/leave id]))}]])

(defn toolbar-content [id display-name color]
  [react/view {:style  {:flex           1
                        :align-items    :center
                        :flex-direction :row}}
   [react/view {:margin-right 10}
    (if (= id constants/status-community-id)
      [react/image {:source (resources/get-image :status-logo)
                    :style {:width 40
                            :height 40}}]
      [chat-icon.screen/chat-icon-view-toolbar
       id
       true
       display-name
       (or color
           (rand-nth colors/chat-colors))])]
   [react/view {:style {:flex 1 :justify-content :center}}
    [react/text {:style {:typography  :main-medium
                         :font-size   15
                         :line-height 22}
                 :number-of-lines     1
                 :accessibility-label :community-name-text}
     display-name]]])

(defn topbar [id display-name color admin joined]
  [topbar/topbar
   {:content           [toolbar-content id display-name color]
    :navigation        {:on-press #(re-frame/dispatch [:navigate-back])}
    :right-accessories (when (or admin joined)
                         [{:icon                :main-icons/more
                           :accessibility-label :community-menu-button
                           :on-press
                           #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                {:content (fn []
                                                            [community-actions id admin])
                                                 :height  256}])}])}])

(defn welcome-blank-page []
  [react/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [react/i18n-text {:style home.styles/welcome-blank-text :key :welcome-blank-message}]])

(views/defview community-unviewed-count [id]
  (views/letsubs [unviewed-count [:communities/unviewed-count id]]
    (when-not (zero? unviewed-count)
      [react/view {:style               {:background-color colors/blue
                                         :border-radius    6
                                         :margin-right     5
                                         :margin-top       2
                                         :width            12
                                         :height           12}
                   :accessibility-label :unviewed-messages-public}])))

(defn status-community [{:keys [id description]}]
  [quo/list-item
   {:icon                      [react/image {:source (resources/get-image :status-logo)
                                             :style {:width 40
                                                     :height 40}}]
    :title                     [react/view {:flex-direction :row
                                            :flex           1}
                                [react/view {:flex-direction :row
                                             :flex           1
                                             :padding-right  16
                                             :align-items    :center}
                                 [quo/text {:weight              :medium
                                            :accessibility-label :chat-name-text
                                            :font-size           17
                                            :ellipsize-mode      :tail
                                            :number-of-lines     1}
                                  (get-in description [:identity :display-name])]]
                                [react/view {:flex-direction :row
                                             :flex           1
                                             :justify-content :flex-end
                                             :align-items    :center}
                                 [community-unviewed-count id]]]

    :title-accessibility-label :chat-name-text
    :on-press                  #(do
                                  (re-frame/dispatch [:dismiss-keyboard])
                                  (re-frame/dispatch [:navigate-to :community id]))
    ;; TODO: actions
    :on-long-press             #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                    nil])}])

(defn channel-preview-item [{:keys [id identity]}]
  [quo/list-item
   {:icon                      [chat-icon.screen/chat-icon-view-chat-list
                                id true (:display-name identity) colors/blue false false]
    :title                     [react/view {:flex-direction :row
                                            :flex           1}
                                [react/view {:flex-direction :row
                                             :flex           1
                                             :padding-right  16
                                             :align-items    :center}
                                 [icons/icon :main-icons/tiny-group
                                  {:color           colors/black
                                   :width           15
                                   :height          15
                                   :container-style {:width           15
                                                     :height          15
                                                     :margin-right   2}}]
                                 [quo/text {:weight              :medium
                                            :accessibility-label :chat-name-text
                                            :ellipsize-mode      :tail
                                            :number-of-lines     1}
                                  (utils/truncate-str (:display-name identity) 30)]]]
    :title-accessibility-label :chat-name-text
    :subtitle                  [react/view {:flex-direction :row}
                                [react/text-class {:style               home.styles/last-message-text
                                                   :number-of-lines     1
                                                   :ellipsize-mode      :tail
                                                   :accessibility-label :chat-message-text} (:description identity)]]}])

(defn community-channel-preview-list [_ description]
  (let [chats (reduce-kv
               (fn [acc k v]
                 (conj acc (assoc v :id (name k))))
               []
               (get-in description [:chats]))]
    [list/flat-list
     {:key-fn                       :id
      :keyboard-should-persist-taps :always
      :data                         chats
      :render-fn                    channel-preview-item}]))

(defn community-chat-list [chats]
  (if (empty? chats)
    [welcome-blank-page]
    [list/flat-list
     {:key-fn                       :chat-id
      :keyboard-should-persist-taps :always
      :data                         chats
      :render-fn                    (fn [home-item] [inner-item/home-list-item (assoc home-item :color colors/blue)])
      :footer                       [react/view {:height 68}]}]))

(views/defview community-channel-list [id]
  (views/letsubs [chats [:chats/by-community-id id]]
    [community-chat-list chats]))

(views/defview community [route]
  (views/letsubs [{:keys [id description joined admin]} [:communities/community (get-in route [:route :params])]]
    [react/view {:style {:flex 1}}
     [topbar
      id
      (get-in description [:identity :display-name])
      (get-in description [:identity :color])
      admin
      joined]
     (if joined
       [community-channel-list id]
       [community-channel-preview-list id description])
     (when-not joined
       [react/view {:style {:padding-top 20
                            :margin-bottom 10
                            :padding-horizontal 20}}
        [quo/button {:on-press #(re-frame/dispatch [::communities/join id])}
         (i18n/label :t/join)]])]))

(views/defview export-community []
  (views/letsubs [{:keys [community-key]}     [:popover/popover]]
    [react/view {}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      [copyable-text/copyable-text-view
       {:label           :t/community-key
        :container-style {:margin-top 12 :margin-bottom 4}
        :copied-text     community-key}
       [quo/text {:number-of-lines     1
                  :ellipsize-mode      :middle
                  :accessibility-label :chat-key
                  :monospace           true}
        community-key]]]]))

(defn render-featured-community [{:keys [name id]}]
  ^{:key id}
  [react/touchable-highlight {:on-press            #(re-frame/dispatch [:navigate-to :community id])
                              :accessibility-label :chat-item}
   [react/view {:padding-right 8 :padding-vertical 8}
    [react/view {:border-color colors/gray-lighter :border-radius 36 :border-width 1 :padding-horizontal 8 :padding-vertical 5}
     [react/text {:style {:color colors/blue :typography :main-medium}} name]]]])

