(ns status-im.ui.screens.communities.views
  (:require
   [quo.core :as quo]
   [status-im.i18n.i18n :as i18n]
   [status-im.utils.core :as utils]
   [status-im.utils.config :as config]
   [status-im.constants :as constants]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt <sub]]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.copyable-text :as copyable-text]
   [status-im.ui.components.topbar :as topbar]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.toolbar :as toolbar]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn hide-sheet-and-dispatch [event]
  (>evt [:bottom-sheet/hide])
  (>evt event))

(defn community-unviewed-count [id]
  (when-not (zero? (<sub [:communities/unviewed-count id]))
    [react/view {:style               {:background-color colors/blue
                                       :border-radius    6
                                       :margin-right     5
                                       :margin-top       2
                                       :width            12
                                       :height           12}
                 :accessibility-label :unviewed-messages-public}]))

(defn community-home-list-item [{:keys [id name last?] :as community}]
  [react/view
   [quo/list-item
    {:icon                      [communities.icon/community-icon community]
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
                                   name]]
                                 [react/view {:flex-direction  :row
                                              :flex            1
                                              :justify-content :flex-end
                                              :align-items     :center}
                                  [community-unviewed-count id]]]
     :title-accessibility-label :chat-name-text
     :on-press                  #(do
                                   (>evt [:dismiss-keyboard])
                                   (>evt [:navigate-to :community {:community-id id}]))}]
     ;; TODO: actions
     ;; :on-long-press             #(>evt [:bottom-sheet/show-sheet
     ;;                                                 nil])

   (when last?
     [quo/separator])])

(defn community-list-item [{:keys [id permissions members name description] :as community}]
  (let [members-count (count members)
        show-members-count? (not= (:access permissions) constants/community-no-membership-access)]
    [quo/list-item
     {:icon                      [communities.icon/community-icon community]
      :title                     [react/view {:flex-direction :row
                                              :flex           1
                                              :padding-right  16
                                              :align-items    :center}
                                  [quo/text {:weight              :medium
                                             :accessibility-label :community-name-text
                                             :ellipsize-mode      :tail
                                             :number-of-lines     1}
                                   (utils/truncate-str name 30)]]
      :title-accessibility-label :community-name-text
      :subtitle                  [react/view
                                  [quo/text {:number-of-lines 1}
                                   description]
                                  [quo/text {:number-of-lines 1
                                             :color           :secondary}
                                   (if show-members-count?
                                     (i18n/label-pluralize members-count :t/community-members {:count members-count})
                                     (i18n/label :t/open-membership))]]
      :on-press                  #(do
                                    (>evt [:dismiss-keyboard])
                                    (>evt [:navigate-to :community {:community-id id}]))}]))

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

(defn communities-home-list [communities]
  [list/flat-list
   {:key-fn                         :id
    :keyboard-should-persist-taps   :always
    :data                       communities
    :render-fn                      community-home-list-item}])

(defn communities-list [communities]
  [list/section-list
   {:content-container-style        {:padding-vertical 8}
    :key-fn                         :id
    :keyboard-should-persist-taps   :always
    :sticky-section-headers-enabled false
    :sections                       communities
    :render-section-header-fn       quo/list-index
    :render-fn                      community-list-item}])

(defn communities []
  (let [communities (<sub [:communities/section-list])]
    [react/view {:flex 1}
     [topbar/topbar (cond-> {:title (i18n/label :t/communities)
                             :modal? true}
                      config/communities-management-enabled?
                      (assoc :right-accessories [{:icon                :main-icons/more
                                                  :accessibility-label :chat-menu-button
                                                  :on-press
                                                  #(>evt [:bottom-sheet/show-sheet
                                                          {:content (fn []
                                                                      [communities-actions])
                                                           :height  256}])}]))]
     [communities-list communities]
     (when config/communities-management-enabled?
       [toolbar/toolbar
        {:show-border? true
         :center       [quo/button {:on-press #(>evt [::communities/open-create-community])
                                    :type     :secondary}
                        (i18n/label :t/create-community)]}])]))

(defn export-community []
  (let [{:keys [community-key]} (<sub [:popover/popover])]
    [react/view {:style {:padding-top        16
                         :padding-horizontal 16}}
     [quo/text {:size  :x-large
                :align :center}
      (i18n/label :t/community-private-key)]
     [copyable-text/copyable-text-view
      {:container-style {:padding-vertical 12}
       :copied-text     community-key}
      [quo/text {:number-of-lines     1
                 :ellipsize-mode      :middle
                 :accessibility-label :chat-key
                 :monospace           true}
       community-key]]]))

(defn render-featured-community [{:keys [name id]}]
  ^{:key id}
  [react/touchable-highlight {:on-press            #(>evt [:navigate-to :community {:community-id id}])
                              :accessibility-label :chat-item}
   [react/view {:padding-right    8
                :padding-vertical 8}
    [react/view {:border-color       colors/gray-lighter
                 :border-radius      36
                 :border-width       1
                 :padding-horizontal 8
                 :padding-vertical   5}
     [quo/text {:color :link} name]]]])

