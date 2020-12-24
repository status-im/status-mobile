(ns status-im.ui.screens.communities.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [quo.core :as quo]
   [status-im.i18n :as i18n]
   [status-im.utils.core :as utils]
   [status-im.utils.config :as config]
   [status-im.constants :as constants]
   [status-im.communities.core :as communities]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.copyable-text :as copyable-text]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.topbar :as topbar]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
   [status-im.ui.components.toolbar :as toolbar]
   [status-im.ui.components.react :as react]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn community-list-item [{:keys [id description]}]
  (let [identity (:identity description)
        members  (count (:members description))]
    [quo/list-item
     {:icon (if (= id constants/status-community-id)
              [react/image {:source (resources/get-image :status-logo)
                            :style  {:width  40
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
                                              :flex           1
                                              :padding-right  16
                                              :align-items    :center}
                                  [quo/text {:weight              :medium
                                             :accessibility-label :community-name-text
                                             :ellipsize-mode      :tail
                                             :number-of-lines     1}
                                   (utils/truncate-str (:display-name identity) 30)]]
      :title-accessibility-label :community-name-text
      :subtitle                  [react/view
                                  [quo/text {:number-of-lines 1}
                                   (:description identity)]
                                  [quo/text {:number-of-lines 1
                                             :color           :secondary}
                                   (i18n/label-pluralize members :t/community-members {:count members})]]
      :on-press                  #(do
                                    (re-frame/dispatch [:dismiss-keyboard])
                                    (re-frame/dispatch [:navigate-to :community {:community-id id}]))}]))

(defn communities-actions []
  [:<>
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/import-community)
     :accessibility-label :community-import-community
     :icon                :main-icons/objects
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :community-import])}]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/create-community)
     :accessibility-label :community-create-community
     :icon                :main-icons/add
     :on-press            #(hide-sheet-and-dispatch [::communities/open-create-community])}]])

(defn communities []
  (let [communities @(re-frame/subscribe [:communities/section-list])]
    [react/view {:flex 1}
     [topbar/topbar (cond-> {:title (i18n/label :t/communities)
                             :modal? true}
                      config/communities-management-enabled?
                      (assoc :right-accessories [{:icon                :main-icons/more
                                                  :accessibility-label :chat-menu-button
                                                  :on-press
                                                  #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                                       {:content (fn []
                                                                                   [communities-actions])
                                                                        :height  256}])}]))]
     [list/section-list
      {:contentContainerStyle          {:padding-bottom 16
                                        :padding-top    8}
       :key-fn                         :id
       :keyboard-should-persist-taps   :always
       :sticky-section-headers-enabled false
       :sections                       communities
       :render-section-header-fn       quo/list-index
       :render-fn                      community-list-item}]
     (when config/communities-management-enabled?
       [toolbar/toolbar
        {:show-border? true
         :center       [quo/button {:on-press #(re-frame/dispatch [::communities/open-create-community])
                                    :type     :secondary}
                        (i18n/label :t/create)]}])]))

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
                                             :style  {:width  40
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
                                [react/view {:flex-direction  :row
                                             :flex            1
                                             :justify-content :flex-end
                                             :align-items     :center}
                                 [community-unviewed-count id]]]
    :title-accessibility-label :chat-name-text
    :on-press                  #(do
                                  (re-frame/dispatch [:dismiss-keyboard])
                                  (re-frame/dispatch [:navigate-to :community {:community-id id}]))
    ;; TODO: actions
    ;; :on-long-press             #(re-frame/dispatch [:bottom-sheet/show-sheet
    ;;                                                 nil])
    }])

(views/defview export-community []
  (views/letsubs [{:keys [community-key]}     [:popover/popover]]
    [react/view {}
     [react/view {:style {:padding-top 16 :padding-horizontal 16}}
      [copyable-text/copyable-text-view
       {:label           :t/community-private-key
        :container-style {:margin-top 12 :margin-bottom 4}
        :copied-text     community-key}
       [quo/text {:number-of-lines     1
                  :ellipsize-mode      :middle
                  :accessibility-label :chat-key
                  :monospace           true}
        community-key]]]]))

(defn render-featured-community [{:keys [name id]}]
  ^{:key id}
  [react/touchable-highlight {:on-press            #(re-frame/dispatch [:navigate-to :community {:community-id id}])
                              :accessibility-label :chat-item}
   [react/view {:padding-right 8 :padding-vertical 8}
    [react/view {:border-color colors/gray-lighter :border-radius 36 :border-width 1 :padding-horizontal 8 :padding-vertical 5}
     [react/text {:style {:color colors/blue :typography :main-medium}} name]]]])

