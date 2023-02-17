(ns status-im.ui.screens.communities.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as quo.colors]
            [status-im.communities.core :as communities]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.ui.components.badge :as badge]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.communities.community :as community]
            [status-im.ui.screens.communities.icon :as communities.icon]
            [status-im.utils.core :as utils]
            [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn community-unviewed-count
  [id]
  (let [{:keys [unviewed-messages-count unviewed-mentions-count]} (rf/sub [:communities/unviewed-counts
                                                                           id])]
    (cond
      (pos? unviewed-mentions-count)
      [badge/message-counter unviewed-mentions-count]

      (pos? unviewed-messages-count)
      [react/view
       {:style               {:background-color quo.colors/blue
                              :border-radius    6
                              :margin-right     5
                              :margin-top       2
                              :width            12
                              :height           12}
        :accessibility-label :unviewed-messages-public}])))

(defn community-home-list-item
  [{:keys [id name last?] :as community}]
  [react/touchable-opacity
   {:style         (merge {:height 64}
                          (when last?
                            {:border-bottom-color (quo.colors/get-color :ui-01)
                             :border-bottom-width 1}))
    :on-press      (fn []
                     (rf/dispatch [:communities/load-category-states id])
                     (rf/dispatch [:dismiss-keyboard])
                     (rf/dispatch [:navigate-to-nav2 :community {:community-id id}]))
    :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                  {:content (fn []
                                              [community/community-actions community])}])}
   [:<>
    [react/view {:top 12 :left 16 :position :absolute}
     [communities.icon/community-icon community]]
    [react/view
     {:style               {:margin-left    72
                            :flex-direction :row
                            :flex           1}
      :accessibility-label :chat-name-text}
     [react/view
      {:flex-direction :row
       :flex           1
       :padding-right  16
       :align-items    :center}
      [quo/text
       {:weight              :medium
        :accessibility-label :chat-name-text
        :font-size           17
        :ellipsize-mode      :tail
        :number-of-lines     1}
       name]]
     [react/view
      {:flex-direction  :row
       :flex            1
       :margin-right    15
       :justify-content :flex-end
       :align-items     :center}
      [community-unviewed-count id]]]]])

(defn community-list-item
  [{:keys [id permissions members name description] :as community}]
  (let [members-count       (count members)
        show-members-count? (not= (:access permissions) constants/community-no-membership-access)]
    [quo/list-item
     {:icon                      [communities.icon/community-icon community]
      :title                     [react/view
                                  {:flex-direction :row
                                   :flex           1
                                   :padding-right  16
                                   :align-items    :center}
                                  [quo/text
                                   {:weight              :medium
                                    :accessibility-label :community-name-text
                                    :ellipsize-mode      :tail
                                    :number-of-lines     1}
                                   (utils/truncate-str name 30)]]
      :title-accessibility-label :community-name-text
      :subtitle                  [react/view
                                  [quo/text {:number-of-lines 1}
                                   description]
                                  [quo/text
                                   {:number-of-lines 1
                                    :color           :secondary}
                                   (if show-members-count?
                                     (i18n/label-pluralize members-count
                                                           :t/community-members
                                                           {:count members-count})
                                     (i18n/label :t/open-membership))]]
      :on-press                  #(do
                                    (rf/dispatch [:dismiss-keyboard])
                                    (rf/dispatch [:navigate-to-nav2 :community {:community-id id}]))}]))

(defn communities-actions
  []
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

(defn communities-list
  [communities]
  [list/section-list
   {:content-container-style        {:padding-vertical 8}
    :key-fn                         :id
    :keyboard-should-persist-taps   :always
    :sticky-section-headers-enabled false
    :sections                       communities
    :render-section-header-fn       quo/list-index
    :render-fn                      community-list-item}])

(defn communities
  []
  (let [communities          (rf/sub [:communities/section-list])
        communities-enabled? (rf/sub [:communities/enabled?])]
    [:<>
     [topbar/topbar
      (cond-> {:title (i18n/label :t/communities)}
        communities-enabled?
        (assoc :right-accessories
               [{:icon                :main-icons/more
                 :accessibility-label :chat-menu-button
                 :on-press
                 #(rf/dispatch [:bottom-sheet/show-sheet
                                {:content (fn []
                                            [communities-actions])
                                 :height  256}])}]))]
     [communities-list communities]
     (when communities-enabled?
       [toolbar/toolbar
        {:show-border? true
         :center       [quo/button
                        {:on-press #(rf/dispatch [::communities/open-create-community])
                         :type     :secondary}
                        (i18n/label :t/create-community)]}])]))

(defn export-community
  []
  (let [{:keys [community-key]} (rf/sub [:popover/popover])]
    [react/view
     {:style {:padding-top        16
              :padding-horizontal 16}}
     [quo/text
      {:size  :x-large
       :align :center}
      (i18n/label :t/community-private-key)]
     [copyable-text/copyable-text-view
      {:container-style {:padding-vertical 12}
       :copied-text     community-key}
      [quo/text
       {:number-of-lines     1
        :ellipsize-mode      :middle
        :accessibility-label :chat-key
        :monospace           true}
       community-key]]]))
